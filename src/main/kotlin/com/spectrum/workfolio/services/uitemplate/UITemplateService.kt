package com.spectrum.workfolio.services.uitemplate

import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.entity.uitemplate.UiTemplatePlan
import com.spectrum.workfolio.domain.entity.uitemplate.UITemplate
import com.spectrum.workfolio.domain.entity.uitemplate.WorkerUITemplate
import com.spectrum.workfolio.domain.enums.UITemplateType
import com.spectrum.workfolio.domain.repository.UiTemplatePlanRepository
import com.spectrum.workfolio.domain.repository.UITemplateRepository
import com.spectrum.workfolio.domain.repository.WorkerRepository
import com.spectrum.workfolio.domain.repository.WorkerUITemplateRepository
import com.spectrum.workfolio.services.CreditService
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class UITemplateService(
    private val uiTemplateRepository: UITemplateRepository,
    private val uiTemplatePlanRepository: UiTemplatePlanRepository,
    private val workerUITemplateRepository: WorkerUITemplateRepository,
    private val workerRepository: WorkerRepository,
    private val creditService: CreditService,
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
    fun getUITemplateByUrlPath(urlPath: String): UITemplate {
        return uiTemplateRepository.findByUrlPathAndIsActiveTrue(urlPath)
            ?: throw WorkfolioException("템플릿을 찾을 수 없습니다.")
    }

    @Transactional(readOnly = true)
    fun getPlansByUiTemplateId(uiTemplateId: String): List<UiTemplatePlan> {
        return uiTemplatePlanRepository.findByUiTemplateIdOrderByDisplayOrderAsc(uiTemplateId)
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

        // Check if already owns valid template
        val existingTemplate = workerUITemplateRepository.findValidByWorkerAndUITemplate(
            worker, uiTemplate, LocalDateTime.now()
        )
        if (existingTemplate != null) {
            throw WorkfolioException("이미 해당 템플릿을 보유하고 있습니다.")
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
    fun hasValidUITemplateByUrlPath(workerId: String, urlPath: String): Boolean {
        val worker = getWorkerById(workerId)
        val uiTemplate = uiTemplateRepository.findByUrlPathAndIsActiveTrue(urlPath) ?: return false
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

    private fun getWorkerById(workerId: String): Worker {
        return workerRepository.findById(workerId)
            .orElseThrow { WorkfolioException("사용자를 찾을 수 없습니다.") }
    }
}
