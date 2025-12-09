package com.spectrum.workfolio.config

import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import java.net.InetAddress
import java.sql.Connection
import java.sql.Statement
import javax.sql.DataSource

@Configuration
@Order(1) // 다른 초기화보다 먼저 실행
class ConnectionInitializer(
    private val dataSource: DataSource,
) : ApplicationListener<ApplicationReadyEvent> {

    private val logger = LoggerFactory.getLogger(ConnectionInitializer::class.java)

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        try {
            logger.info("🔄 연결 초기화 시작...")

            // 현재 서버의 IP 주소 확인
            val localIp = InetAddress.getLocalHost().hostAddress
            logger.info("📍 현재 서버 IP: $localIp")

            // HikariCP DataSource인 경우
            if (dataSource is HikariDataSource) {
                val hikariDataSource = dataSource

                // 연결 풀 상태 확인
                logger.info("🔌 기존 연결 풀 상태 확인 중...")
                hikariDataSource.hikariPoolMXBean?.let { pool ->
                    val activeConnections = pool.activeConnections
                    val idleConnections = pool.idleConnections
                    val totalConnections = pool.totalConnections

                    logger.info(
                        "📊 연결 풀 상태 - Active: $activeConnections, Idle: $idleConnections, Total: $totalConnections"
                    )

                    // PostgreSQL에서 현재 사용자의 오래된 연결 종료
                    terminateStaleConnections(hikariDataSource)
                }

                // 연결 풀의 모든 연결을 새로 생성하도록 유도
                logger.info("🔄 연결 풀 갱신 중...")
                refreshConnectionPool(hikariDataSource)

                // 새로운 연결 생성 테스트
                logger.info("✅ 새로운 연결 생성 테스트 중...")
                dataSource.connection.use { connection ->
                    val isValid = connection.isValid(5) // 5초 타임아웃
                    if (isValid) {
                        logger.info("✅ 연결 검증 성공 - 새로운 연결이 정상적으로 생성되었습니다")
                    } else {
                        logger.warn("⚠️ 연결 검증 실패")
                    }
                }

                // 최종 연결 풀 상태
                hikariDataSource.hikariPoolMXBean?.let { pool ->
                    logger.info(
                        "📊 최종 연결 풀 상태 - Active: ${pool.activeConnections}, Idle: ${pool.idleConnections}, Total: ${pool.totalConnections}"
                    )
                }

                logger.info("✅ 연결 초기화 완료")
            } else {
                logger.warn("⚠️ HikariCP DataSource가 아닙니다. 연결 초기화를 건너뜁니다.")
            }
        } catch (e: Exception) {
            logger.error("❌ 연결 초기화 중 오류 발생", e)
            // 오류가 발생해도 애플리케이션은 계속 실행되도록 함
        }
    }

    /**
     * PostgreSQL에서 현재 사용자의 모든 기존 연결을 종료 (현재 세션 제외)
     * 서버 시작 시 EC2에서 생성한 모든 기존 연결을 제거하고 새로 생성하기 위함
     */
    private fun terminateStaleConnections(dataSource: HikariDataSource) {
        var connection: Connection? = null
        var statement: Statement? = null

        try {
            connection = dataSource.connection
            statement = connection.createStatement()

            // 현재 사용자의 모든 연결 중 현재 세션을 제외한 모든 연결 종료
            // 서버 시작 시점이므로 기존 연결은 모두 정리해야 함
            val selectSql = """
                SELECT pid, client_addr, state, query_start, state_change
                FROM pg_stat_activity
                WHERE datname = current_database()
                  AND usename = current_user
                  AND pid != pg_backend_pid()
            """.trimIndent()

            val pidList = mutableListOf<Long>()
            statement.executeQuery(selectSql).use { resultSet ->
                while (resultSet.next()) {
                    val pid = resultSet.getLong("pid")
                    val clientAddr = resultSet.getString("client_addr")
                    val state = resultSet.getString("state")
                    pidList.add(pid)
                    logger.debug("발견된 연결 - PID: $pid, Client: $clientAddr, State: $state")
                }
            }

            if (pidList.isEmpty()) {
                logger.info("ℹ️ 종료할 기존 연결이 없습니다 (새로운 서버 시작)")
                return
            }

            logger.info("🔌 ${pidList.size}개의 기존 연결을 종료합니다 (서버 재시작으로 인한 정리)...")

            // 각 PID에 대해 연결 종료
            var terminatedCount = 0
            for (pid in pidList) {
                try {
                    val terminateSql = "SELECT pg_terminate_backend($pid)"
                    statement.executeQuery(terminateSql).use { resultSet ->
                        if (resultSet.next() && resultSet.getBoolean(1)) {
                            terminatedCount++
                            logger.debug("✅ PID $pid 연결 종료 성공")
                        }
                    }
                } catch (e: Exception) {
                    logger.debug("PID $pid 종료 실패 (이미 종료되었을 수 있음): ${e.message}")
                }
            }

            logger.info("✅ 종료된 연결 수: $terminatedCount / ${pidList.size}")

            // 잠시 대기하여 연결 종료가 완료되도록 함
            Thread.sleep(500)
        } catch (e: Exception) {
            logger.warn("⚠️ 기존 연결 종료 중 오류 발생 (무시하고 계속 진행): ${e.message}")
        } finally {
            statement?.close()
            connection?.close()
        }
    }

    /**
     * 연결 풀의 연결을 갱신 (기존 연결을 점진적으로 교체)
     */
    private fun refreshConnectionPool(dataSource: HikariDataSource) {
        try {
            val pool = dataSource.hikariPoolMXBean
            if (pool != null) {
                val idleBefore = pool.idleConnections
                logger.info("📊 Idle 연결 수: $idleBefore")

                // HikariCP는 max-lifetime 설정에 따라 자동으로 연결을 교체합니다
                // 여기서는 연결 풀이 정상적으로 작동하는지 확인만 합니다
                logger.info("ℹ️ HikariCP가 max-lifetime 설정에 따라 자동으로 연결을 교체합니다")
            }
        } catch (e: Exception) {
            logger.debug("연결 풀 갱신 중 오류 (정상 동작 가능): ${e.message}")
        }
    }
}
