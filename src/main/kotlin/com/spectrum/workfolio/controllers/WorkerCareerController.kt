package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.worker_career.WorkerCareerUpdateRequest
import com.spectrum.workfolio.services.WorkerCareerService
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workers/career")
class WorkerCareerController(
    private val workerCareerService: WorkerCareerService,
) {

    @PutMapping
    fun updateCareer(
        @AuthenticatedUser workerId: String,
        @RequestBody request: WorkerCareerUpdateRequest
    ): SuccessResponse {
        workerCareerService.updateCareer(workerId, request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
