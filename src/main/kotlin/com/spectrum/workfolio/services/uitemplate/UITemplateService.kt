package com.spectrum.workfolio.services.uitemplate

import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.entity.uitemplate.UiTemplatePlan
import com.spectrum.workfolio.domain.entity.uitemplate.UITemplate
import com.spectrum.workfolio.domain.entity.uitemplate.UITemplateImage
import com.spectrum.workfolio.domain.entity.uitemplate.WorkerUITemplate
import com.spectrum.workfolio.domain.enums.UITemplateImageType
import com.spectrum.workfolio.domain.enums.UITemplateType
import com.spectrum.workfolio.domain.repository.UiTemplatePlanRepository
import com.spectrum.workfolio.domain.repository.UITemplateRepository
import com.spectrum.workfolio.domain.repository.UITemplateImageRepository
import com.spectrum.workfolio.domain.repository.WorkerRepository
import com.spectrum.workfolio.domain.repository.WorkerUITemplateRepository
import com.spectrum.workfolio.services.CreditService
import com.spectrum.workfolio.services.SupabaseStorageService
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.UUID

@Service
class UITemplateService(
    private val uiTemplateRepository: UITemplateRepository,
    private val uiTemplatePlanRepository: UiTemplatePlanRepository,
    private val uiTemplateImageRepository: UITemplateImageRepository,
    private val workerUITemplateRepository: WorkerUITemplateRepository,
    private val workerRepository: WorkerRepository,
    private val creditService: CreditService,
    private val supabaseStorageService: SupabaseStorageService,
) {

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
    fun getImagesByUiTemplateId(uiTemplateId: String): List<UITemplateImage> {
        return uiTemplateImageRepository.findByUiTemplateIdOrderByDisplayOrderAsc(uiTemplateId)
    }

    @Transactional(readOnly = true)
    fun getImagesByUiTemplateIdAndType(uiTemplateId: String, imageType: UITemplateImageType): List<UITemplateImage> {
        return uiTemplateImageRepository.findByUiTemplateIdAndImageTypeOrderByDisplayOrderAsc(uiTemplateId, imageType)
    }

    // ==================== Authenticated API ====================

    @Transactional
    fun purchaseUITemplate(workerId: String, uiTemplateId: String, planId: String? = null): WorkerUITemplate {
        val worker = getWorkerById(workerId)
        val uiTemplate = getUITemplateById(uiTemplateId)

        val (price: Int, durationDays: Int) = if (planId != null) {
            val plan = uiTemplatePlanRepository.findByIdAndUiTemplateId(planId, uiTemplateId)
                ?: throw WorkfolioException("선택한 이용 기간 옵션을 찾을 수 없습니다.")
            Pair(plan.price, plan.durationDays)
        } else {
            Pair(uiTemplate.price, uiTemplate.durationDays)
        }

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
            description = "${uiTemplate.name} 템플릿 구매"
        )

        // Check if already owns valid template → extend expiration
        val existingTemplate = workerUITemplateRepository.findValidByWorkerAndUITemplate(
            worker, uiTemplate, LocalDateTime.now()
        )
        if (existingTemplate != null) {
            existingTemplate.extendExpiration(durationDays)
            return workerUITemplateRepository.save(existingTemplate)
        }

        // Create worker ui template
        val now = LocalDateTime.now()
        val workerUITemplate = WorkerUITemplate(
            worker = worker,
            uiTemplate = uiTemplate,
            purchasedAt = now,
            expiredAt = now.plusDays(durationDays.toLong()),
            creditsUsed = price,
            isActive = true
        )

        return workerUITemplateRepository.save(workerUITemplate)
    }

    @Transactional(readOnly = true)
    fun getMyUITemplates(workerId: String, pageable: Pageable): Page<WorkerUITemplate> {
        val worker = getWorkerById(workerId)
        return workerUITemplateRepository.findByWorkerAndIsActiveTrueOrderByPurchasedAtDesc(worker, pageable)
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

        worker.setDefaultUiTemplate(uiTemplate)
        workerRepository.save(worker)

        return fetchDefaultUITemplates(worker)
    }

    @Transactional(readOnly = true)
    fun getDefaultUITemplates(workerId: String): Pair<UITemplate?, UITemplate?> {
        val worker = getWorkerById(workerId)
        return fetchDefaultUITemplates(worker)
    }

    /**
     * Worker의 기본 템플릿을 트랜잭션 내에서 명시적으로 조회하여 LazyInitializationException 방지
     */
    private fun fetchDefaultUITemplates(worker: Worker): Pair<UITemplate?, UITemplate?> {
        val urlTemplate = worker.defaultUrlUiTemplate?.let { uiTemplateRepository.findById(it.id).orElse(null) }
        val pdfTemplate = worker.defaultPdfUiTemplate?.let { uiTemplateRepository.findById(it.id).orElse(null) }
        return Pair(urlTemplate, pdfTemplate)
    }

    @Transactional
    fun deleteMyUITemplate(workerId: String, workerUITemplateId: String) {
        val workerUITemplate = workerUITemplateRepository.findById(workerUITemplateId)
            .orElseThrow { WorkfolioException("보유 템플릿을 찾을 수 없습니다.") }

        if (workerUITemplate.worker.id != workerId) {
            throw WorkfolioException("해당 템플릿에 대한 접근 권한이 없습니다.")
        }

        // Clear default template if this template was set as default
        val worker = workerUITemplate.worker
        val uiTemplate = workerUITemplate.uiTemplate
        val isDefaultUrl = worker.defaultUrlUiTemplate?.id == uiTemplate.id
        val isDefaultPdf = worker.defaultPdfUiTemplate?.id == uiTemplate.id

        if (isDefaultUrl) {
            worker.clearDefaultUiTemplate(UITemplateType.URL)
            workerRepository.save(worker)
        }
        if (isDefaultPdf) {
            worker.clearDefaultUiTemplate(UITemplateType.PDF)
            workerRepository.save(worker)
        }

        workerUITemplate.deactivate()
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
        isPopular: Boolean,
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
            isPopular = isPopular,
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
        isPopular: Boolean,
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
            isPopular = isPopular,
            displayOrder = displayOrder,
        )
        return uiTemplateRepository.save(uiTemplate)
    }

    @Transactional
    fun deleteUITemplate(uiTemplateId: String) {
        val uiTemplate = getUITemplateByIdForAdmin(uiTemplateId)
        uiTemplate.deactivate()
        uiTemplateRepository.save(uiTemplate)
    }

    @Transactional
    fun uploadTemplateImages(
        uiTemplateId: String,
        files: List<MultipartFile>,
        imageType: UITemplateImageType,
    ): List<UITemplateImage> {
        val uiTemplate = getUITemplateByIdForAdmin(uiTemplateId)
        val existingImages = uiTemplateImageRepository.findByUiTemplateIdOrderByDisplayOrderAsc(uiTemplateId)
        var nextOrder = if (existingImages.isEmpty()) 0 else existingImages.maxOf { it.displayOrder } + 1

        return files.map { file ->
            val fileName = "${UUID.randomUUID()}_${file.originalFilename}"
            val storagePath = "ui-templates/$uiTemplateId"
            val imageUrl = supabaseStorageService.uploadFile(file, fileName, storagePath)

            val image = UITemplateImage(
                uiTemplate = uiTemplate,
                imageType = imageType,
                imageUrl = imageUrl,
                displayOrder = nextOrder++,
            )
            uiTemplateImageRepository.save(image)
        }
    }

    @Transactional
    fun deleteTemplateImage(imageId: String) {
        val image = uiTemplateImageRepository.findById(imageId)
            .orElseThrow { WorkfolioException("이미지를 찾을 수 없습니다.") }
        supabaseStorageService.deleteFileByUrl(image.imageUrl)
        uiTemplateImageRepository.delete(image)
    }

    private fun getWorkerById(workerId: String): Worker {
        return workerRepository.findById(workerId)
            .orElseThrow { WorkfolioException("사용자를 찾을 수 없습니다.") }
    }
}
