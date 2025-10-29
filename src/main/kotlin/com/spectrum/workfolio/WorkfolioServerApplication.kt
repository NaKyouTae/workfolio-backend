package com.spectrum.workfolio

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class WorkfolioServerApplication

fun main(args: Array<String>) {
    runApplication<WorkfolioServerApplication>(*args)
}
