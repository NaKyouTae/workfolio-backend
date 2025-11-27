package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.release.ReleaseNoticeListResponse
import com.spectrum.workfolio.proto.release.ReleasePlanListResponse
import com.spectrum.workfolio.services.ReleaseService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/release")
class ReleaseController(
    private val releaseService: ReleaseService,
) {

    @GetMapping("/notices")
    fun getNotices(): ReleaseNoticeListResponse {
        return releaseService.getNotices()
    }

    @GetMapping("/plans")
    fun getPlans(): ReleasePlanListResponse {
        return releaseService.getPlans()
    }
}
