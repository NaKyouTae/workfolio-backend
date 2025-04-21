package com.spectrum.workfolio.config

import com.zaxxer.hikari.HikariDataSource
import liquibase.integration.spring.SpringLiquibase
import org.springframework.beans.factory.annotation.Value
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
import javax.sql.DataSource

@Configuration
@EnableJpaRepositories(
    basePackages = ["com.spectrum.workfolio"],
    entityManagerFactoryRef = "primaryEntityManager",
    transactionManagerRef = "primaryTransactionManager"
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
                setGenerateDdl(true)
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
                )
            )
            jpaPropertyMap.putAll(
                HibernateProperties().determineHibernateProperties(
                    JpaProperties().properties,
                    HibernateSettings(),
                )
            )
        }

        return em
    }

    @Bean
    @Primary
    fun primaryTransactionManager(): PlatformTransactionManager {
        return JpaTransactionManager().apply {
            entityManagerFactory = primaryEntityManager().`object`
        }
    }

    @Bean
    fun primaryLiquibase(): SpringLiquibase {
        val liquibase = SpringLiquibase()
        liquibase.changeLog = "classpath:/db/primary/db.changelog-master.yaml"
        liquibase.dataSource = primaryDataSource()
        liquibase.setShouldRun(true)
        return liquibase
    }
}

