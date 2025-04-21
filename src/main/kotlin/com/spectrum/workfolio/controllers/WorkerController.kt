package com.spectrum.workfolio.controllers

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workers")
class WorkerController {

    @PostMapping("")
    fun test(): String {
        return "Hello World"
    }
}
