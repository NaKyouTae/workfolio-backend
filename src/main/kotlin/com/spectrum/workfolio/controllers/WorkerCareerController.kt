package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.common.Company
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.worker_career.WorkerCareerListResponse
import com.spectrum.workfolio.proto.worker_career.WorkerCareerUpdateRequest
import com.spectrum.workfolio.services.WorkerCareerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workers/careers")
class WorkerCareerController(
    private val workerCareerService: WorkerCareerService,
) {

    @GetMapping
    fun getCareers(
        @AuthenticatedUser workerId: String,
    ): WorkerCareerListResponse {
        return workerCareerService.listCareers(workerId)
    }

    @PutMapping
    fun updateCareers(
        @AuthenticatedUser workerId: String,
        @RequestBody request: WorkerCareerUpdateRequest
    ): SuccessResponse {
        workerCareerService.updateCareer(workerId, request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
