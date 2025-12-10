package com.spectrum.workfolio.config

import com.zaxxer.hikari.HikariDataSource
import liquibase.integration.spring.SpringLiquibase
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
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

    @Value("\${spring.datasource.hikari.minimum-idle:2}")
    private var minimumIdle: Int = 2

    @Value("\${spring.datasource.hikari.maximum-pool-size:10}")
    private var maximumPoolSize: Int = 10

    @Value("\${spring.datasource.hikari.idle-timeout:0}")
    private var idleTimeout: Long = 0

    @Bean
    @Primary
    fun primaryDataSource(): DataSource {
        val properties = DataSourceProperties()

        properties.driverClassName = driverClassName
        properties.url = jdbcUrl
        properties.username = username
        properties.password = password

        val dataSource = properties
            .initializeDataSourceBuilder()
            .type(HikariDataSource::class.java)
            .build() as HikariDataSource

        // HikariCP 설정 명시적으로 적용 (application.properties의 설정이 제대로 적용되도록 보장)
        // initializeDataSourceBuilder()가 자동으로 읽지만, 명시적으로 설정하여 확실히 적용
        dataSource.poolName = "WorkfolioHikariPool"
        dataSource.minimumIdle = minimumIdle
        dataSource.maximumPoolSize = maximumPoolSize
        dataSource.idleTimeout = idleTimeout

        // 설정 확인 로그 (디버깅용)
        println("=== HikariCP 설정 확인 ===")
        println("Pool Name: ${dataSource.poolName}")
        println("Minimum Idle: ${dataSource.minimumIdle}")
        println("Maximum Pool Size: ${dataSource.maximumPoolSize}")
        println("Idle Timeout: ${dataSource.idleTimeout} (0 = 무제한)")
        println("=========================")

        return dataSource
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
            defaultTimeout = 30 // 30초 전역 트랜잭션 타임아웃 설정 (초 단위)
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
