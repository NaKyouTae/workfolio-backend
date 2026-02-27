package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.services.admin.AdminDashboardService
import com.spectrum.workfolio.services.admin.AdminDashboardStatsResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/dashboard")
class AdminDashboardController(
    private val adminDashboardService: AdminDashboardService,
) {
    @GetMapping("/stats")
    fun getStats(): AdminDashboardStatsResponse {
        return adminDashboardService.getStats()
    }
}
