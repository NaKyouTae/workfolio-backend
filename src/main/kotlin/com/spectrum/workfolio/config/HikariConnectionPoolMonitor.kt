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
     * 
     * ⚠️ 주의: minimum-idle 설정에 따라 idle Connection이 유지되는 것은 정상입니다.
     * - minimum-idle=2: 최소 2개 Connection 유지 (정상)
     * - maximum-pool-size=10: 최대 10개 Connection
     * 
     * idle Connection이 많다고 해서 문제가 되는 것은 아닙니다.
     * 문제는 "idle in transaction" 상태이거나 Connection이 반환되지 않는 경우입니다.
     */
    private fun analyzeIdleConnections(idleCount: Int) {
        // Active Connection이 0이고 Idle Connection만 있는 경우는 정상 상태입니다.
        // minimum-idle 설정에 따라 Connection이 유지되는 것이므로 경고를 출력하지 않습니다.
        val active = if (dataSource is HikariDataSource) {
            dataSource.hikariPoolMXBean.activeConnections
        } else {
            0
        }
        
        // Active Connection이 없고 Idle만 있는 경우는 정상 상태
        if (active == 0 && idleCount > 0) {
            logger.debug(
                """
                |✅ Connection Pool 정상 상태
                |Active: 0, Idle: $idleCount
                |minimum-idle 설정에 따라 Connection이 유지되고 있습니다. (정상)
                """.trimMargin()
            )
            return
        }
        
        // Active Connection이 있는데 Idle도 많은 경우만 경고
        // (Connection이 제대로 반환되지 않을 수 있음)
        if (idleCount >= 5 && active > 0) {
            logger.warn(
                """
                |⚠️ Idle Connection이 ${idleCount}개 유지되고 있습니다. (Active: $active)
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
    
    /**
     * 오래된 Idle Connection 강제 정리
     * 
     * ⚠️ 중요: HikariCP Housekeeper와의 차이점
     * - Housekeeper: HikariCP Pool 레벨에서 Connection 제거 (약 30초마다)
     *   → Connection.close() 호출하지만, PostgreSQL 레벨에서는 닫히지 않을 수 있음
     * - 이 스케줄러: PostgreSQL 레벨에서 Connection 종료 (2분마다)
     *   → pg_terminate_backend()로 실제로 종료
     * 
     * 따라서 겹치지 않으며, Housekeeper가 제거한 Connection이 PostgreSQL에서
     * 여전히 idle 상태로 유지되는 경우를 처리합니다.
     */
    @Scheduled(fixedRate = 120000, initialDelay = 60000) // 2분마다 실행, 시작 후 1분 대기
    fun evictIdleConnections() {
        if (dataSource is HikariDataSource) {
            try {
                val pool = dataSource.hikariPoolMXBean
                val idle = pool.idleConnections
                val total = pool.totalConnections
                val maxPoolSize = dataSource.maximumPoolSize
                
                // HikariCP의 Connection 수와 PostgreSQL의 Connection 수 비교
                val postgresConnectionCount = getPostgreSQLConnectionCount()
                
                if (postgresConnectionCount > total) {
                    val diff = postgresConnectionCount - total
                    logger.warn(
                        """
                        |⚠️ Connection 불일치 감지!
                        |HikariCP Connection 수: $total
                        |PostgreSQL Connection 수: $postgresConnectionCount
                        |차이: ${diff}개
                        |
                        |원인: HikariCP가 Connection을 제거했다고 판단했지만,
                        |PostgreSQL 레벨에서는 Connection이 여전히 idle 상태로 유지되고 있습니다.
                        |이는 Supabase Transaction Pooler의 동작 방식 때문입니다.
                        """.trimMargin()
                    )
                }
                
                // PostgreSQL Connection 수가 HikariCP보다 많은 경우만 정리
                // (Housekeeper가 이미 처리한 경우는 제외)
                if (postgresConnectionCount > total) {
                    val diff = postgresConnectionCount - total
                    logger.warn(
                        """
                        |🧹 오래된 Idle Connection 강제 정리 시작
                        |현재 상태: HikariCP Idle=$idle, Total=$total, Max=$maxPoolSize
                        |PostgreSQL Connection 수: $postgresConnectionCount (차이: ${diff}개)
                        |
                        |💡 Housekeeper가 Connection을 제거했다고 판단했지만,
                        |PostgreSQL 레벨에서는 여전히 idle 상태로 유지되고 있습니다.
                        |PostgreSQL에서 직접 Connection 종료 시도
                        """.trimMargin()
                    )
                    
                    // PostgreSQL에서 직접 Connection 종료 시도
                    try {
                        val jdbcTemplate = org.springframework.jdbc.core.JdbcTemplate(dataSource)
                        
                        // HikariCP가 제거했다고 판단한 Connection만 종료
                        // (idle-timeout=3분보다 약간 긴 4분 이상 idle 상태인 Connection)
                        // 이렇게 하면 Housekeeper가 방금 제거한 Connection은 제외하고,
                        // 실제로 닫히지 않은 오래된 Connection만 종료
                        val terminatedCount = jdbcTemplate.queryForList(
                            """
                            SELECT pg_terminate_backend(pid) as terminated
                            FROM pg_stat_activity
                            WHERE datname = 'postgres'
                              AND state = 'idle'
                              AND application_name LIKE 'workfolio-server-%'
                              AND NOW() - query_start > INTERVAL '4 minutes'
                              AND pid != pg_backend_pid()
                            """.trimIndent(),
                            Map::class.java
                        ).size
                        
                        if (terminatedCount > 0) {
                            logger.info("✅ ${terminatedCount}개의 오래된 Idle Connection을 PostgreSQL에서 종료했습니다.")
                            logger.info("💡 HikariCP Connection 수와 PostgreSQL Connection 수를 동기화했습니다.")
                        } else {
                            logger.debug("종료할 Connection이 없습니다. (Housekeeper가 이미 처리했을 수 있음)")
                        }
                    } catch (e: Exception) {
                        logger.error(
                            "⚠️ PostgreSQL에서 Connection 종료 실패: ${e.message}. " +
                            "Supabase에서 권한이 제한되어 있을 수 있습니다.",
                            e
                        )
                    }
                } else if (idle > 3 || total >= maxPoolSize * 0.8) {
                    // HikariCP Pool이 거의 가득 찬 경우만 경고 (Housekeeper가 처리할 것)
                    logger.debug(
                        """
                        |HikariCP Pool 상태: Idle=$idle, Total=$total, Max=$maxPoolSize
                        |Housekeeper가 자동으로 정리할 예정입니다.
                        """.trimMargin()
                    )
                }
            } catch (e: Exception) {
                logger.error("Idle Connection 정리 중 오류: ${e.message}", e)
            }
        }
    }
    
    /**
     * PostgreSQL에서 현재 애플리케이션의 Connection 수 조회
     */
    private fun getPostgreSQLConnectionCount(): Int {
        return try {
            val jdbcTemplate = org.springframework.jdbc.core.JdbcTemplate(dataSource)
            jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM pg_stat_activity
                WHERE datname = 'postgres'
                  AND application_name LIKE 'workfolio-server-%'
                  AND state = 'idle'
                """.trimIndent(),
                Int::class.java
            ) ?: 0
        } catch (e: Exception) {
            logger.debug("PostgreSQL Connection 수 조회 실패: ${e.message}")
            0
        }
    }
    
}

