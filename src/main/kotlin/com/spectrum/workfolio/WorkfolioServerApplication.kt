package com.spectrum.workfolio

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WorkfolioServerApplication

fun main(args: Array<String>) {
    runApplication<WorkfolioServerApplication>(*args)
}
