package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.certifications.CertificationsCreateRequest
import com.spectrum.workfolio.proto.certifications.CertificationsListResponse
import com.spectrum.workfolio.proto.certifications.CertificationsUpdateRequest
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.services.CertificationsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workers/certifications")
class CertificationsController(
    private val certificationsService: CertificationsService,
) {

    @GetMapping
    fun getCertifications(
        @AuthenticatedUser workerId: String,
    ): CertificationsListResponse {
        return certificationsService.listCertifications(workerId)
    }

    @PostMapping
    fun createCertifications(
        @AuthenticatedUser workerId: String,
        @RequestBody request: CertificationsCreateRequest,
    ): SuccessResponse {
        certificationsService.createCertifications(workerId, request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updateCertifications(
        @RequestBody request: CertificationsUpdateRequest,
    ): SuccessResponse {
        certificationsService.updateCertifications(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
