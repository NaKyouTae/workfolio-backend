package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.domain.enums.UITemplateType
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.proto.uitemplate.*
import com.spectrum.workfolio.services.CreditService
import com.spectrum.workfolio.services.uitemplate.UITemplateService
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/ui-templates")
class UITemplateController(
    private val uiTemplateService: UITemplateService,
    private val creditService: CreditService,
) {

    @PostMapping("/purchase")
    fun purchaseUITemplate(
        @AuthenticatedUser workerId: String,
        @RequestBody request: UITemplatePurchaseRequest,
    ): UITemplatePurchaseResponse {
        val planId = if (request.hasPlanId()) request.planId else null
        val workerUITemplate = uiTemplateService.purchaseUITemplate(workerId, request.uiTemplateId, planId)
        val balanceAfter = creditService.getBalance(workerId)

        return UITemplatePurchaseResponse.newBuilder()
            .setWorkerUiTemplate(workerUITemplate.toProto())
            .setCreditsUsed(workerUITemplate.creditsUsed)
            .setBalanceAfter(balanceAfter)
            .build()
    }

    @GetMapping("/my")
    fun getMyUITemplates(
        @AuthenticatedUser workerId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): WorkerUITemplateListResponse {
        val pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "purchasedAt"))
        val workerUITemplatesPage = uiTemplateService.getMyUITemplates(workerId, pageable)

        return WorkerUITemplateListResponse.newBuilder()
            .addAllWorkerUiTemplates(workerUITemplatesPage.content.map { it.toProto() })
            .setTotalElements(workerUITemplatesPage.totalElements.toInt())
            .setTotalPages(workerUITemplatesPage.totalPages)
            .setCurrentPage(page)
            .build()
    }

    @GetMapping("/my/{workerUITemplateId}")
    fun getMyUITemplateDetail(
        @AuthenticatedUser workerId: String,
        @PathVariable workerUITemplateId: String,
    ): WorkerUITemplateGetResponse {
        val workerUITemplate = uiTemplateService.getWorkerUITemplateDetail(workerId, workerUITemplateId)
        return WorkerUITemplateGetResponse.newBuilder()
            .setWorkerUiTemplate(workerUITemplate.toProto())
            .build()
    }

    @GetMapping("/my/active")
    fun getMyActiveUITemplates(
        @AuthenticatedUser workerId: String,
        @RequestParam(required = false) type: String?,
    ): ActiveUITemplatesResponse {
        val workerUITemplates = if (type != null) {
            val uiTemplateType = UITemplateType.valueOf(type)
            uiTemplateService.getMyActiveUITemplatesByType(workerId, uiTemplateType)
        } else {
            uiTemplateService.getMyActiveUITemplates(workerId)
        }

        return ActiveUITemplatesResponse.newBuilder()
            .addAllWorkerUiTemplates(workerUITemplates.map { it.toProto() })
            .build()
    }

    @GetMapping("/check/{uiTemplateId}")
    fun checkUITemplateOwnership(
        @AuthenticatedUser workerId: String,
        @PathVariable uiTemplateId: String,
    ): UITemplateOwnershipResponse {
        val workerUITemplate = uiTemplateService.checkUITemplateOwnership(workerId, uiTemplateId)

        val builder = UITemplateOwnershipResponse.newBuilder()
            .setOwnsUiTemplate(workerUITemplate != null)

        if (workerUITemplate != null) {
            builder.setWorkerUiTemplate(workerUITemplate.toProto())
        }

        return builder.build()
    }

}
