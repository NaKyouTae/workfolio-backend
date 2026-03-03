package com.spectrum.workfolio.services.uitemplate

import com.spectrum.workfolio.domain.entity.Image
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.entity.uitemplate.UiTemplatePlan
import com.spectrum.workfolio.domain.entity.uitemplate.UITemplate
import com.spectrum.workfolio.domain.entity.uitemplate.WorkerUITemplate
import com.spectrum.workfolio.domain.enums.ImageExtType
import com.spectrum.workfolio.domain.enums.ImageTargetType
import com.spectrum.workfolio.domain.enums.UITemplateType
import com.spectrum.workfolio.domain.repository.UiTemplatePlanRepository
import com.spectrum.workfolio.domain.repository.UITemplateRepository
import com.spectrum.workfolio.domain.repository.WorkerRepository
import com.spectrum.workfolio.domain.repository.WorkerUITemplateRepository
import com.spectrum.workfolio.services.CreditService
import com.spectrum.workfolio.services.ImageService
import com.spectrum.workfolio.utils.BusinessEventLogger
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class UITemplateService(
    private val uiTemplateRepository: UITemplateRepository,
    private val uiTemplatePlanRepository: UiTemplatePlanRepository,
    private val workerUITemplateRepository: WorkerUITemplateRepository,
    private val workerRepository: WorkerRepository,
    private val creditService: CreditService,
    private val imageService: ImageService,
) {
    private val periodFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd")

    // ==================== Public API (No Auth Required) ====================

    @Transactional(readOnly = true)
    fun getAllActiveUITemplates(): List<UITemplate> {
        return uiTemplateRepository.findAllByIsActiveTrueOrderByDisplayOrderAsc()
    }

    @Transactional(readOnly = true)
    fun getActiveUITemplatesByType(type: UITemplateType): List<UITemplate> {
        return uiTemplateRepository.findAllByTypeAndIsActiveTrueOrderByDisplayOrderAsc(type)
    }

    @Transactional(readOnly = true)
    fun getUITemplateById(uiTemplateId: String): UITemplate {
        return uiTemplateRepository.findByIdAndIsActiveTrue(uiTemplateId)
            ?: throw WorkfolioException("템플릿을 찾을 수 없습니다.")
    }

    @Transactional(readOnly = true)
    fun getPlansByUiTemplateId(uiTemplateId: String): List<UiTemplatePlan> {
        return uiTemplatePlanRepository.findByUiTemplateIdOrderByDisplayOrderAsc(uiTemplateId)
    }

    @Transactional(readOnly = true)
    fun getImagesByUiTemplateId(uiTemplateId: String): List<Image> {
        return imageService.getImagesByTarget(ImageTargetType.UI_TEMPLATE, uiTemplateId)
    }

    @Transactional(readOnly = true)
    fun getImagesByUiTemplateIdAndType(uiTemplateId: String, extType: ImageExtType): List<Image> {
        return imageService.getImagesByTargetAndExtType(ImageTargetType.UI_TEMPLATE, uiTemplateId, extType)
    }

    // ==================== Authenticated API ====================

    @Transactional
    fun purchaseUITemplate(workerId: String, uiTemplateId: String, planId: String? = null): WorkerUITemplate {
        val worker = getWorkerById(workerId)
        val uiTemplate = getUITemplateById(uiTemplateId)
        val now = LocalDateTime.now()

        val (price: Int, durationDays: Int) = if (planId != null) {
            val plan = uiTemplatePlanRepository.findByIdAndUiTemplateId(planId, uiTemplateId)
                ?: throw WorkfolioException("선택한 이용 기간 옵션을 찾을 수 없습니다.")
            Pair(plan.price, plan.durationDays)
        } else {
            Pair(uiTemplate.price, uiTemplate.durationDays)
        }

        // Check if already owns valid template → extend expiration
        val existingTemplate = workerUITemplateRepository.findValidByWorkerAndUITemplate(
            worker, uiTemplate, now
        )

        // Check if there's an expired template → reactivate instead of creating new row
        val expiredTemplate = if (existingTemplate == null) {
            workerUITemplateRepository.findExpiredByWorkerAndUITemplate(worker, uiTemplate, now)
        } else null

        val periodStart = existingTemplate?.expiredAt ?: now
        val periodEnd = periodStart.plusDays(durationDays.toLong())
        val actionText = when {
            existingTemplate != null -> "연장"
            expiredTemplate != null -> "재구매"
            else -> "구매"
        }
        val creditHistoryDescription =
            "${uiTemplate.name} 템플릿 $actionText (${durationDays}일, ${periodStart.format(periodFormatter)} ~ ${periodEnd.format(periodFormatter)})"

        // Check and use credits
        if (!worker.hasEnoughCredits(price)) {
            throw WorkfolioException("크레딧이 부족합니다.")
        }

        // Use credits
        creditService.useCredits(
            workerId = workerId,
            amount = price,
            referenceType = "UI_TEMPLATE",
            referenceId = uiTemplateId,
            description = creditHistoryDescription,
        )
        if (existingTemplate != null) {
            existingTemplate.extendExpiration(durationDays)
            val saved = workerUITemplateRepository.save(existingTemplate)
            BusinessEventLogger.logEvent(
                eventType = "TEMPLATE_PURCHASED",
                message = "템플릿 연장: templateId=$uiTemplateId, workerId=$workerId",
                workerId = workerId,
                templateId = uiTemplateId,
                amount = price,
                status = "EXTENDED",
                extra = mapOf("duration_days" to durationDays.toString(), "template_name" to uiTemplate.name),
            )
            return saved
        }

        // Reactivate expired template
        if (expiredTemplate != null) {
            expiredTemplate.reactivate(now, durationDays, price)
            val saved = workerUITemplateRepository.save(expiredTemplate)
            BusinessEventLogger.logEvent(
                eventType = "TEMPLATE_PURCHASED",
                message = "템플릿 재구매: templateId=$uiTemplateId, workerId=$workerId",
                workerId = workerId,
                templateId = uiTemplateId,
                amount = price,
                status = "REACTIVATED",
                extra = mapOf("duration_days" to durationDays.toString(), "template_name" to uiTemplate.name),
            )
            return saved
        }

        // Create worker ui template
        val workerUITemplate = WorkerUITemplate(
            worker = worker,
            uiTemplate = uiTemplate,
            purchasedAt = now,
            expiredAt = now.plusDays(durationDays.toLong()),
            creditsUsed = price,
            templateType = uiTemplate.type,
        )

        val saved = workerUITemplateRepository.save(workerUITemplate)
        BusinessEventLogger.logEvent(
            eventType = "TEMPLATE_PURCHASED",
            message = "템플릿 신규 구매: templateId=$uiTemplateId, workerId=$workerId",
            workerId = workerId,
            templateId = uiTemplateId,
            amount = price,
            status = "NEW_PURCHASE",
            extra = mapOf("duration_days" to durationDays.toString(), "template_name" to uiTemplate.name),
        )
        return saved
    }

    @Transactional(readOnly = true)
    fun getMyUITemplates(workerId: String, pageable: Pageable): Page<WorkerUITemplate> {
        val worker = getWorkerById(workerId)
        return workerUITemplateRepository.findByWorkerAndStatusActiveOrderByPurchasedAtDesc(worker, pageable)
    }

    @Transactional(readOnly = true)
    fun getMyActiveUITemplates(workerId: String): List<WorkerUITemplate> {
        val worker = getWorkerById(workerId)
        return workerUITemplateRepository.findActiveByWorker(worker, LocalDateTime.now())
    }

    @Transactional(readOnly = true)
    fun getMyActiveUITemplatesByType(workerId: String, type: UITemplateType): List<WorkerUITemplate> {
        val worker = getWorkerById(workerId)
        return workerUITemplateRepository.findValidByWorkerAndType(worker, type, LocalDateTime.now())
    }

    @Transactional(readOnly = true)
    fun hasValidUITemplate(workerId: String, uiTemplateId: String): Boolean {
        val worker = getWorkerById(workerId)
        val uiTemplate = uiTemplateRepository.findById(uiTemplateId).orElse(null) ?: return false
        return workerUITemplateRepository.hasValidUITemplate(worker, uiTemplate, LocalDateTime.now())
    }

    @Transactional(readOnly = true)
    fun getWorkerUITemplateDetail(workerId: String, workerUITemplateId: String): WorkerUITemplate {
        val workerUITemplate = workerUITemplateRepository.findById(workerUITemplateId)
            .orElseThrow { WorkfolioException("보유 템플릿을 찾을 수 없습니다.") }

        if (workerUITemplate.worker.id != workerId) {
            throw WorkfolioException("해당 템플릿에 대한 접근 권한이 없습니다.")
        }

        return workerUITemplate
    }

    @Transactional(readOnly = true)
    fun checkUITemplateOwnership(workerId: String, uiTemplateId: String): WorkerUITemplate? {
        val worker = getWorkerById(workerId)
        val uiTemplate = uiTemplateRepository.findById(uiTemplateId).orElse(null) ?: return null
        return workerUITemplateRepository.findValidByWorkerAndUITemplate(worker, uiTemplate, LocalDateTime.now())
    }

    @Transactional
    fun setDefaultUITemplate(workerId: String, uiTemplateId: String): Pair<UITemplate?, UITemplate?> {
        val worker = getWorkerById(workerId)
        val uiTemplate = getUITemplateById(uiTemplateId)

        // Verify ownership
        val ownership = workerUITemplateRepository.findValidByWorkerAndUITemplate(
            worker, uiTemplate, LocalDateTime.now()
        ) ?: throw WorkfolioException("해당 템플릿을 보유하고 있지 않습니다.")

        if (!ownership.isValid()) {
            throw WorkfolioException("만료되었거나 비활성화된 템플릿입니다.")
        }

        // Clear existing default for this worker + type
        workerUITemplateRepository.clearDefaultByWorkerAndType(worker, uiTemplate.type)

        // Set new default
        ownership.markAsDefault()
        workerUITemplateRepository.save(ownership)

        return fetchDefaultUITemplates(worker)
    }

    @Transactional(readOnly = true)
    fun getDefaultUITemplates(workerId: String): Pair<UITemplate?, UITemplate?> {
        val worker = getWorkerById(workerId)
        return fetchDefaultUITemplates(worker)
    }

    private fun fetchDefaultUITemplates(worker: Worker): Pair<UITemplate?, UITemplate?> {
        val urlDefault = workerUITemplateRepository.findDefaultByWorkerAndType(worker, UITemplateType.URL)
        val pdfDefault = workerUITemplateRepository.findDefaultByWorkerAndType(worker, UITemplateType.PDF)
        return Pair(urlDefault?.uiTemplate, pdfDefault?.uiTemplate)
    }

    @Transactional
    fun deleteMyUITemplate(workerId: String, workerUITemplateId: String) {
        val workerUITemplate = workerUITemplateRepository.findById(workerUITemplateId)
            .orElseThrow { WorkfolioException("보유 템플릿을 찾을 수 없습니다.") }

        if (workerUITemplate.worker.id != workerId) {
            throw WorkfolioException("해당 템플릿에 대한 접근 권한이 없습니다.")
        }

        if (workerUITemplate.isDefault) {
            workerUITemplate.clearDefault()
        }

        workerUITemplate.softDelete()
        workerUITemplateRepository.save(workerUITemplate)
    }

    // ==================== Admin API ====================

    @Transactional(readOnly = true)
    fun getAllUITemplatesForAdmin(): List<UITemplate> {
        return uiTemplateRepository.findAllByOrderByDisplayOrderAsc()
    }

    @Transactional(readOnly = true)
    fun getUITemplateByIdForAdmin(uiTemplateId: String): UITemplate {
        return uiTemplateRepository.findById(uiTemplateId)
            .orElseThrow { WorkfolioException("템플릿을 찾을 수 없습니다.") }
    }

    @Transactional
    fun createUITemplate(
        name: String,
        description: String?,
        type: UITemplateType,
        label: String?,
        price: Int,
        durationDays: Int,
        urlPath: String?,
        isActive: Boolean,
        displayOrder: Int,
    ): UITemplate {
        val uiTemplate = UITemplate(
            name = name,
            description = description,
            type = type,
            label = label,
            price = price,
            durationDays = durationDays,
            urlPath = urlPath,
            isActive = isActive,
            displayOrder = displayOrder,
        )
        return uiTemplateRepository.save(uiTemplate)
    }

    @Transactional
    fun updateUITemplate(
        uiTemplateId: String,
        name: String,
        description: String?,
        type: UITemplateType,
        label: String?,
        price: Int,
        durationDays: Int,
        urlPath: String?,
        isActive: Boolean,
        displayOrder: Int,
    ): UITemplate {
        val uiTemplate = getUITemplateByIdForAdmin(uiTemplateId)
        uiTemplate.changeInfo(
            name = name,
            description = description,
            type = type,
            label = label,
            price = price,
            durationDays = durationDays,
            urlPath = urlPath,
            isActive = isActive,
            displayOrder = displayOrder,
        )
        return uiTemplateRepository.save(uiTemplate)
    }

    @Transactional
    fun deleteUITemplate(uiTemplateId: String) {
        val uiTemplate = getUITemplateByIdForAdmin(uiTemplateId)
        workerUITemplateRepository.clearDefaultByUiTemplateId(uiTemplateId)

        imageService.deleteImagesByTarget(ImageTargetType.UI_TEMPLATE, uiTemplateId)

        uiTemplatePlanRepository.deleteByUiTemplateId(uiTemplateId)
        workerUITemplateRepository.deleteByUiTemplateId(uiTemplateId)
        uiTemplateRepository.delete(uiTemplate)
    }

    // ==================== Admin Plan CRUD ====================

    @Transactional
    fun createPlan(uiTemplateId: String, durationDays: Int, price: Int, displayOrder: Int): UiTemplatePlan {
        val uiTemplate = getUITemplateByIdForAdmin(uiTemplateId)
        val plan = UiTemplatePlan(
            uiTemplate = uiTemplate,
            durationDays = durationDays,
            price = price,
            displayOrder = displayOrder,
        )
        return uiTemplatePlanRepository.save(plan)
    }

    @Transactional
    fun updatePlan(planId: String, durationDays: Int, price: Int, displayOrder: Int): UiTemplatePlan {
        val plan = uiTemplatePlanRepository.findById(planId)
            .orElseThrow { WorkfolioException("플랜을 찾을 수 없습니다.") }
        plan.changeInfo(durationDays, price, displayOrder)
        return uiTemplatePlanRepository.save(plan)
    }

    @Transactional
    fun deletePlan(planId: String) {
        val plan = uiTemplatePlanRepository.findById(planId)
            .orElseThrow { WorkfolioException("플랜을 찾을 수 없습니다.") }
        uiTemplatePlanRepository.delete(plan)
    }

    @Transactional
    fun uploadTemplateImages(
        uiTemplateId: String,
        files: List<MultipartFile>,
        extType: ImageExtType,
    ): List<Image> {
        getUITemplateByIdForAdmin(uiTemplateId) // validate template exists
        return imageService.uploadImages(
            targetType = ImageTargetType.UI_TEMPLATE,
            targetId = uiTemplateId,
            extType = extType,
            files = files,
        )
    }

    @Transactional
    fun deleteTemplateImage(imageId: String) {
        imageService.deleteImage(imageId)
    }

    private fun getWorkerById(workerId: String): Worker {
        return workerRepository.findById(workerId)
            .orElseThrow { WorkfolioException("사용자를 찾을 수 없습니다.") }
    }
}
