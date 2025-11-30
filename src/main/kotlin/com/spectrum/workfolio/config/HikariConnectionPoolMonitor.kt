package com.spectrum.workfolio.config

import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.sql.DataSource

/**
 * HikariCP Connection Pool 모니터링 컴포넌트
 * Connection leak 및 pool 상태를 주기적으로 모니터링
 * Connection 생성 원인 추적 기능 포함
 */
@Component
class HikariConnectionPoolMonitor(
    private val dataSource: DataSource,
) {
    private val logger = LoggerFactory.getLogger(HikariConnectionPoolMonitor::class.java)
    
    private var previousTotal = 0
    private var previousActive = 0
    private var previousIdle = 0

    /**
     * 30초마다 Connection Pool 상태를 로깅
     */
    @Scheduled(fixedRate = 30000) // 30초마다 실행
    fun monitorConnectionPool() {
        if (dataSource is HikariDataSource) {
            val pool = dataSource.hikariPoolMXBean
            
            val active = pool.activeConnections
            val idle = pool.idleConnections
            val total = pool.totalConnections
            val threadsAwaiting = pool.threadsAwaitingConnection
            val maxPoolSize = dataSource.maximumPoolSize
            
            // Connection 변화 추적
            val totalChange = total - previousTotal
            val activeChange = active - previousActive
            val idleChange = idle - previousIdle

            logger.info(
                """
                |=== HikariCP Connection Pool Status ===
                |Active Connections: $active / $maxPoolSize ${if (activeChange != 0) "(${if (activeChange > 0) "+" else ""}$activeChange)" else ""}
                |Idle Connections: $idle ${if (idleChange != 0) "(${if (idleChange > 0) "+" else ""}$idleChange)" else ""}
                |Total Connections: $total / $maxPoolSize ${if (totalChange != 0) "(${if (totalChange > 0) "+" else ""}$totalChange)" else ""}
                |Threads Awaiting Connection: $threadsAwaiting
                |Pool Usage: ${(active.toDouble() / maxPoolSize * 100).toInt()}%
                |========================================
                """.trimMargin()
            )

            // Connection 변화 분석
            if (totalChange < 0) {
                logger.info(
                    "📉 Connection이 ${-totalChange}개 감소했습니다. (HikariCP가 idle-timeout 또는 max-lifetime에 따라 정리함)"
                )
            } else if (totalChange > 0) {
                logger.info(
                    "📈 Connection이 ${totalChange}개 증가했습니다. (새로운 요청으로 인해 생성됨)"
                )
            }

            // 경고: Connection Pool이 80% 이상 사용 중
            if (active >= maxPoolSize * 0.8) {
                logger.warn(
                    "⚠️ Connection Pool usage is high: $active / $maxPoolSize (${(active.toDouble() / maxPoolSize * 100).toInt()}%)"
                )
            }

            // 경고: Connection을 기다리는 스레드가 있음
            if (threadsAwaiting > 0) {
                logger.warn(
                    "⚠️ $threadsAwaiting thread(s) are waiting for a connection!"
                )
            }

            // 경고: 모든 Connection이 사용 중
            if (active >= maxPoolSize && threadsAwaiting > 0) {
                logger.error(
                    "❌ Connection Pool exhausted! All $maxPoolSize connections are in use, $threadsAwaiting thread(s) waiting!"
                )
            }
            
            // 경고: Connection이 계속 증가하는 경우 (Connection leak 가능성)
            if (totalChange > 0 && total >= maxPoolSize * 0.9) {
                logger.warn(
                    "⚠️ Connection이 계속 증가하고 있습니다. Connection leak 가능성을 확인하세요. (현재: $total / $maxPoolSize)"
                )
            }
            
            // Connection 생성 시 상세 정보 로깅
            if (totalChange > 0) {
                logConnectionCreationDetails(totalChange)
            }
            
            // Idle Connection이 오래 유지되는 경우 분석
            if (idle > 0 && idleChange == 0 && totalChange == 0) {
                analyzeIdleConnections(idle)
            }
            
            // 이전 값 저장
            previousTotal = total
            previousActive = active
            previousIdle = idle
        }
    }
    
    /**
     * Connection 생성 시점의 상세 정보 로깅
     */
    private fun logConnectionCreationDetails(count: Int) {
        try {
            val currentThread = Thread.currentThread()
            val stackTrace = currentThread.stackTrace
            
            // 호출 스택에서 의미 있는 정보 추출 (최대 10줄)
            val relevantStackTrace = stackTrace
                .take(15)
                .filter { 
                    !it.className.contains("HikariConnectionPoolMonitor") &&
                    !it.className.contains("Scheduled") &&
                    !it.className.contains("Thread") &&
                    !it.className.contains("jdk.internal")
                }
                .take(10)
                .map { "${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})" }
            
            logger.info(
                """
                |🔍 Connection 생성 감지: ${count}개
                |Thread: ${currentThread.name}
                |호출 스택:
                |${relevantStackTrace.joinToString("\n") { "  → $it" }}
                """.trimMargin()
            )
        } catch (e: Exception) {
            logger.debug("Connection 생성 정보 추적 실패: ${e.message}")
        }
    }
    
    /**
     * Idle Connection 분석
     */
    private fun analyzeIdleConnections(idleCount: Int) {
        if (idleCount >= 5) {
            logger.warn(
                """
                |⚠️ Idle Connection이 ${idleCount}개 유지되고 있습니다.
                |가능한 원인:
                |1. 트랜잭션이 완료되지 않아 Connection이 반환되지 않음
                |2. 외부 API 호출 등으로 인한 긴 트랜잭션
                |3. Connection leak (트랜잭션 종료 후 Connection 미반환)
                |
                |확인 방법:
                |- HikariCP 로그에서 "Connection leak detection" 메시지 확인
                |- pg_stat_activity에서 해당 Connection의 state와 query 확인
                |- 트랜잭션 로그에서 타임아웃 발생 여부 확인
                """.trimMargin()
            )
        }
    }
    
    /**
     * 현재 활성 Connection의 상세 정보 조회 (디버깅용)
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    fun logActiveConnectionDetails() {
        if (dataSource is HikariDataSource) {
            try {
                val pool = dataSource.hikariPoolMXBean
                val active = pool.activeConnections
                
                if (active > 0) {
                    logger.debug(
                        """
                        |📊 활성 Connection 상세 정보:
                        |Active: $active
                        |Idle: ${pool.idleConnections}
                        |Total: ${pool.totalConnections}
                        |Threads Awaiting: ${pool.threadsAwaitingConnection}
                        |
                        |💡 Connection 생성 원인 확인:
                        |- HikariCP DEBUG 로그에서 "Connection added" 메시지 확인
                        |- 트랜잭션 로그에서 "@Transactional" 메서드 호출 확인
                        |- 스레드 덤프로 Connection을 점유한 스레드 확인
                        """.trimMargin()
                    )
                }
            } catch (e: Exception) {
                logger.debug("Connection 상세 정보 조회 실패: ${e.message}")
            }
        }
    }
    
}

