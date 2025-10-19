package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.worker.SystemConfigCreateRequest
import com.spectrum.workfolio.proto.worker.SystemConfigGetResponse
import com.spectrum.workfolio.proto.worker.SystemConfigUpdateRequest
import com.spectrum.workfolio.services.SystemConfigService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/system-configs")
class SystemConfigController(
    private val systemConfigService: SystemConfigService,
) {

    @GetMapping("/{type}")
    fun getSystemConfig(
        @AuthenticatedUser workerId: String,
        @PathVariable type: String,
    ): SystemConfigGetResponse {
        return systemConfigService.getSystemConfigResult(type, workerId)
    }

    @PostMapping
    fun createSystemConfig(
        @RequestBody request: SystemConfigCreateRequest,
    ): SuccessResponse {
        systemConfigService.createSystemConfig(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updateSystemConfig(
        @RequestBody request: SystemConfigUpdateRequest,
    ): SuccessResponse {
        systemConfigService.updateSystemConfig(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
