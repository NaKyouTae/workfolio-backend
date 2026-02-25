package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.uitemplate.UITemplate
import com.spectrum.workfolio.domain.enums.UITemplateType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UITemplateRepository : JpaRepository<UITemplate, String> {
    fun findByIdAndIsActiveTrue(id: String): UITemplate?
    fun findAllByIsActiveTrueOrderByDisplayOrderAsc(): List<UITemplate>
    fun findAllByTypeAndIsActiveTrueOrderByDisplayOrderAsc(type: UITemplateType): List<UITemplate>
    fun findAllByOrderByDisplayOrderAsc(): List<UITemplate>
}
