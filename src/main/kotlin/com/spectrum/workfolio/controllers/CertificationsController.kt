package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.certifications.CertificationsCreateRequest
import com.spectrum.workfolio.proto.certifications.CertificationsListResponse
import com.spectrum.workfolio.proto.certifications.CertificationsResponse
import com.spectrum.workfolio.proto.certifications.CertificationsUpdateRequest
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
    fun listCertifications(
        @AuthenticatedUser workerId: String,
    ): CertificationsListResponse {
        return certificationsService.listCertifications(workerId)
    }

    @PostMapping
    fun createCertifications(
        @AuthenticatedUser workerId: String,
        @RequestBody request: CertificationsCreateRequest,
    ): CertificationsResponse {
        return certificationsService.createCertifications(workerId, request)
    }

    @PutMapping
    fun updateCertifications(
        @RequestBody request: CertificationsUpdateRequest,
    ): CertificationsResponse {
        return certificationsService.updateCertifications(request)
    }
}
