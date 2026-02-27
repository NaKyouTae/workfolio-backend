package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.domain.enums.UITemplateImageType
import com.spectrum.workfolio.domain.enums.UITemplateType
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.uitemplate.*
import com.spectrum.workfolio.services.uitemplate.UITemplateService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin/ui-templates")
class AdminUITemplateController(
    private val uiTemplateService: UITemplateService,
) {

    @GetMapping
    fun getAllUITemplates(): UITemplateListResponse {
        val uiTemplates = uiTemplateService.getAllUITemplatesForAdmin()
        return UITemplateListResponse.newBuilder()
            .addAllUiTemplates(uiTemplates.map { t ->
                val plans = uiTemplateService.getPlansByUiTemplateId(t.id)
                val images = uiTemplateService.getImagesByUiTemplateId(t.id)
                t.toProto(plans, images)
            })
            .build()
    }

    @GetMapping("/{id}")
    fun getUITemplate(@PathVariable id: String): UITemplateGetResponse {
        val uiTemplate = uiTemplateService.getUITemplateByIdForAdmin(id)
        val plans = uiTemplateService.getPlansByUiTemplateId(id)
        val images = uiTemplateService.getImagesByUiTemplateId(id)
        return UITemplateGetResponse.newBuilder()
            .setUiTemplate(uiTemplate.toProto(plans, images))
            .build()
    }

    @PostMapping
    fun createUITemplate(@RequestBody request: AdminUITemplateCreateRequest): UITemplateGetResponse {
        val type = UITemplateType.valueOf(request.type)
        val uiTemplate = uiTemplateService.createUITemplate(
            name = request.name,
            description = if (request.hasDescription()) request.description else null,
            type = type,
            label = if (request.hasLabel()) request.label else null,
            price = request.price,
            durationDays = request.durationDays,
            urlPath = if (request.hasUrlPath()) request.urlPath else null,
            isActive = request.isActive,
            isPopular = request.isPopular,
            displayOrder = request.displayOrder,
        )
        return UITemplateGetResponse.newBuilder()
            .setUiTemplate(uiTemplate.toProto())
            .build()
    }

    @PutMapping("/{id}")
    fun updateUITemplate(
        @PathVariable id: String,
        @RequestBody request: AdminUITemplateUpdateRequest,
    ): UITemplateGetResponse {
        val type = UITemplateType.valueOf(request.type)
        val uiTemplate = uiTemplateService.updateUITemplate(
            uiTemplateId = id,
            name = request.name,
            description = if (request.hasDescription()) request.description else null,
            type = type,
            label = if (request.hasLabel()) request.label else null,
            price = request.price,
            durationDays = request.durationDays,
            urlPath = if (request.hasUrlPath()) request.urlPath else null,
            isActive = request.isActive,
            isPopular = request.isPopular,
            displayOrder = request.displayOrder,
        )
        val plans = uiTemplateService.getPlansByUiTemplateId(id)
        val images = uiTemplateService.getImagesByUiTemplateId(id)
        return UITemplateGetResponse.newBuilder()
            .setUiTemplate(uiTemplate.toProto(plans, images))
            .build()
    }

    @DeleteMapping("/{id}")
    fun deleteUITemplate(@PathVariable id: String): SuccessResponse {
        uiTemplateService.deleteUITemplate(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PostMapping("/{id}/images")
    fun uploadImages(
        @PathVariable id: String,
        @RequestParam("files") files: List<MultipartFile>,
        @RequestParam(defaultValue = "DETAIL") imageType: String,
    ): AdminUITemplateImageListResponse {
        val type = UITemplateImageType.valueOf(imageType)
        val images = uiTemplateService.uploadTemplateImages(id, files, type)
        return AdminUITemplateImageListResponse.newBuilder()
            .addAllImages(images.map { it.toProto() })
            .build()
    }

    @DeleteMapping("/images/{imageId}")
    fun deleteImage(@PathVariable imageId: String): SuccessResponse {
        uiTemplateService.deleteTemplateImage(imageId)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    // ==================== Plan CRUD ====================

    @PostMapping("/{id}/plans")
    fun createPlan(
        @PathVariable id: String,
        @RequestBody request: AdminUiTemplatePlanCreateRequest,
    ): AdminUiTemplatePlanGetResponse {
        val plan = uiTemplateService.createPlan(
            uiTemplateId = id,
            durationDays = request.durationDays,
            price = request.price,
            displayOrder = request.displayOrder,
        )
        return AdminUiTemplatePlanGetResponse.newBuilder()
            .setPlan(plan.toProto())
            .build()
    }

    @PutMapping("/plans/{planId}")
    fun updatePlan(
        @PathVariable planId: String,
        @RequestBody request: AdminUiTemplatePlanUpdateRequest,
    ): AdminUiTemplatePlanGetResponse {
        val plan = uiTemplateService.updatePlan(
            planId = planId,
            durationDays = request.durationDays,
            price = request.price,
            displayOrder = request.displayOrder,
        )
        return AdminUiTemplatePlanGetResponse.newBuilder()
            .setPlan(plan.toProto())
            .build()
    }

    @DeleteMapping("/plans/{planId}")
    fun deletePlan(@PathVariable planId: String): SuccessResponse {
        uiTemplateService.deletePlan(planId)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
