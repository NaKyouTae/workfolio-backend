package com.spectrum.workfolio.config

import com.zaxxer.hikari.HikariDataSource
import jakarta.annotation.PostConstruct
import liquibase.integration.spring.SpringLiquibase
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    basePackages = ["com.spectrum.workfolio"],
    entityManagerFactoryRef = "primaryEntityManager",
    transactionManagerRef = "primaryTransactionManager",
)
class PrimaryDataSourceConfig {
    @Value("\${spring.jpa.database-platform}")
    private lateinit var hibernateDialect: String

    @Value("\${spring.jpa.properties.hibernate.jdbc.batch_size}")
    private lateinit var batchSize: String

    @Value("\${spring.jpa.properties.hibernate.default_batch_fetch_size}")
    private lateinit var batchFetchSize: String

    @Value("\${spring.jpa.properties.hibernate.order_inserts}")
    private lateinit var orderInserts: String

    @Value("\${spring.jpa.properties.hibernate.order_deletes}")
    private lateinit var orderDeletes: String

    @Value("\${spring.jpa.properties.hibernate.order_updates}")
    private lateinit var orderUpdates: String

    @Value("\${spring.jpa.show-sql}")
    private lateinit var showSql: String

    @Value("\${spring.datasource.hikari.driver-class-name}")
    private lateinit var driverClassName: String

    @Value("\${spring.datasource.hikari.jdbc-url}")
    private lateinit var jdbcUrl: String

    @Value("\${spring.datasource.username}")
    private lateinit var username: String

    @Value("\${spring.datasource.password}")
    private lateinit var password: String

    private val logger = LoggerFactory.getLogger(PrimaryDataSourceConfig::class.java)

    /**
     * 애플리케이션 시작 시 PostgreSQL 설정 자동 적용
     * idle_in_transaction_session_timeout 설정으로 Connection leak 방지
     */
    @PostConstruct
    fun configurePostgreSQLTimeout() {
        try {
            val dataSource = primaryDataSource()
            val jdbcTemplate = JdbcTemplate(dataSource)
            
            // 현재 설정 확인
            val currentTimeout = jdbcTemplate.queryForObject(
                "SHOW idle_in_transaction_session_timeout",
                String::class.java
            )
            logger.info("Current idle_in_transaction_session_timeout: $currentTimeout")
            
            // 5분 이상 idle 상태인 트랜잭션 자동 종료 설정
            // 주의: Supabase에서는 데이터베이스 레벨 설정이 제한될 수 있음
            // 세션 레벨 설정은 현재 세션에만 적용되므로, 각 Connection마다 설정 필요
            try {
                jdbcTemplate.execute("SET idle_in_transaction_session_timeout = '5min'")
                logger.info("✅ PostgreSQL idle_in_transaction_session_timeout 설정 완료: 5min")
            } catch (e: Exception) {
                logger.warn(
                    "⚠️ PostgreSQL idle_in_transaction_session_timeout 설정 실패: ${e.message}. " +
                    "Supabase에서 권한이 제한되어 있을 수 있습니다. " +
                    "Supabase 대시보드에서 직접 설정하거나 지원팀에 문의하세요."
                )
            }
        } catch (e: Exception) {
            logger.error("PostgreSQL 설정 적용 중 오류: ${e.message}", e)
        }
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    fun primaryDataSource(): DataSource {
        val properties = DataSourceProperties()

        properties.driverClassName = driverClassName
        properties.url = jdbcUrl
        properties.username = username
        properties.password = password

        return properties
            .initializeDataSourceBuilder()
            .type(HikariDataSource::class.java)
            .build()
    }

    @Bean
    @Primary
    fun primaryEntityManager(): LocalContainerEntityManagerFactoryBean {
        val em = LocalContainerEntityManagerFactoryBean().apply {
            dataSource = primaryDataSource()
            setPackagesToScan("com.spectrum.workfolio.domain.entity", "com.spectrum.workfolio.domain.repository")

            val vendorAdapter = HibernateJpaVendorAdapter().apply {
                setShowSql(showSql.toBoolean())
                setGenerateDdl(false) // Liquibase로만 스키마 관리
            }
            jpaVendorAdapter = vendorAdapter

            jpaPropertyMap.putAll(
                mapOf(
                    "hibernate.dialect" to hibernateDialect,
                    "hibernate.jdbc.batch_size" to batchSize,
                    "hibernate.default_batch_fetch_size" to batchFetchSize,
                    "hibernate.order_inserts" to orderInserts,
                    "hibernate.order_deletes" to orderDeletes,
                    "hibernate.order_updates" to orderUpdates,
                ),
            )
            jpaPropertyMap.putAll(
                HibernateProperties().determineHibernateProperties(
                    JpaProperties().properties,
                    HibernateSettings(),
                ),
            )
        }

        return em
    }

    @Bean
    @Primary
    fun primaryTransactionManager(): PlatformTransactionManager {
        return JpaTransactionManager().apply {
            entityManagerFactory = primaryEntityManager().`object`
            // 전역 트랜잭션 타임아웃 설정 (초 단위)
            // 모든 @Transactional 메서드에 기본적으로 적용됨
            // 개별 메서드에서 timeout을 지정하면 그 값이 우선 적용됨
            defaultTimeout = 30  // 30초
        }
    }

    @Bean
    @ConditionalOnProperty(name = ["spring.liquibase.enabled"], havingValue = "true", matchIfMissing = true)
    fun primaryLiquibase(): SpringLiquibase {
        val liquibase = SpringLiquibase()
        liquibase.changeLog = "classpath:/db/primary/db.changelog-master.yaml"
        liquibase.dataSource = primaryDataSource()
        liquibase.setShouldRun(true)
        return liquibase
    }
}
