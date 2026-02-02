package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.uitemplate.UITemplate
import com.spectrum.workfolio.domain.entity.uitemplate.UiTemplatePlan
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UiTemplatePlanRepository : JpaRepository<UiTemplatePlan, String> {
    fun findByUiTemplateOrderByDisplayOrderAsc(uiTemplate: UITemplate): List<UiTemplatePlan>
    fun findByUiTemplateIdOrderByDisplayOrderAsc(uiTemplateId: String): List<UiTemplatePlan>
    fun findByIdAndUiTemplateId(id: String, uiTemplateId: String): UiTemplatePlan?
}
