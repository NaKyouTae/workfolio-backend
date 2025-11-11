package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.feature.FeatureCreateRequest
import com.spectrum.workfolio.proto.feature.FeatureGetResponse
import com.spectrum.workfolio.proto.feature.FeatureListResponse
import com.spectrum.workfolio.proto.feature.FeatureUpdateRequest
import com.spectrum.workfolio.services.plan.FeatureCommandService
import com.spectrum.workfolio.services.plan.FeatureQueryService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/features")
class FeatureController(
    private val featureQueryService: FeatureQueryService,
    private val featureCommandService: FeatureCommandService,
) {

    @GetMapping
    fun listFeatures(
        @RequestParam(required = false) domain: String?,
    ): FeatureListResponse {
        return if (domain != null) {
            featureQueryService.listFeaturesByDomainResult(domain)
        } else {
            featureQueryService.listFeaturesResult()
        }
    }

    @GetMapping("/{id}")
    fun getFeature(
        @PathVariable id: String,
    ): FeatureGetResponse {
        return featureQueryService.getFeatureResult(id)
    }

    @PostMapping
    fun createFeature(
        @RequestBody request: FeatureCreateRequest,
    ): SuccessResponse {
        featureCommandService.createFeature(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updateFeature(
        @RequestBody request: FeatureUpdateRequest,
    ): SuccessResponse {
        featureCommandService.updateFeature(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @DeleteMapping("/{id}")
    fun deleteFeature(
        @PathVariable id: String,
    ): SuccessResponse {
        featureCommandService.deleteFeature(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
