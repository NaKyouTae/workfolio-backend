package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.career.CareerCreateRequest
import com.spectrum.workfolio.proto.career.CareerListResponse
import com.spectrum.workfolio.proto.career.CareerResponse
import com.spectrum.workfolio.proto.career.CareerUpdateRequest
import com.spectrum.workfolio.services.CareerService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/careers")
class CareerController(
    private val careerService: CareerService,
) {

    @GetMapping
    fun listCareers(
        @AuthenticatedUser workerId: String,
    ): CareerListResponse {
        return careerService.listCareers(workerId)
    }

    @PostMapping
    fun createCareer(
        @RequestBody request: CareerCreateRequest,
    ): CareerResponse {
        return careerService.createCareer(request)
    }

    @PutMapping
    fun updateCareer(
        @RequestBody request: CareerUpdateRequest,
    ): CareerResponse {
        return careerService.updateCareer(request)
    }
}
