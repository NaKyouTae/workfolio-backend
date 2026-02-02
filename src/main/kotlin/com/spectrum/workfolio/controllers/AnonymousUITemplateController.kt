package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.domain.enums.UITemplateType
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.proto.uitemplate.UITemplateGetResponse
import com.spectrum.workfolio.proto.uitemplate.UITemplateListResponse
import com.spectrum.workfolio.services.uitemplate.UITemplateService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/anonymous/ui-templates")
class AnonymousUITemplateController(
    private val uiTemplateService: UITemplateService,
) {

    @GetMapping
    fun getAllUITemplates(
        @RequestParam(required = false) type: String?,
    ): UITemplateListResponse {
        val uiTemplates = if (type != null) {
            val uiTemplateType = UITemplateType.valueOf(type)
            uiTemplateService.getActiveUITemplatesByType(uiTemplateType)
        } else {
            uiTemplateService.getAllActiveUITemplates()
        }

        return UITemplateListResponse.newBuilder()
            .addAllUiTemplates(uiTemplates.map { t ->
                val plans = uiTemplateService.getPlansByUiTemplateId(t.id)
                t.toProto(plans)
            })
            .build()
    }

    @GetMapping("/{uiTemplateId}")
    fun getUITemplate(
        @PathVariable uiTemplateId: String,
    ): UITemplateGetResponse {
        val uiTemplate = uiTemplateService.getUITemplateById(uiTemplateId)
        val plans = uiTemplateService.getPlansByUiTemplateId(uiTemplateId)
        return UITemplateGetResponse.newBuilder()
            .setUiTemplate(uiTemplate.toProto(plans))
            .build()
    }

    @GetMapping("/url-path/{urlPath}")
    fun getUITemplateByUrlPath(
        @PathVariable urlPath: String,
    ): UITemplateGetResponse {
        val uiTemplate = uiTemplateService.getUITemplateByUrlPath(urlPath)
        val plans = uiTemplateService.getPlansByUiTemplateId(uiTemplate.id)
        return UITemplateGetResponse.newBuilder()
            .setUiTemplate(uiTemplate.toProto(plans))
            .build()
    }
}
