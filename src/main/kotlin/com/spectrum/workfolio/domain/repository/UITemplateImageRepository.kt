package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.uitemplate.UITemplateImage
import com.spectrum.workfolio.domain.enums.UITemplateImageType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UITemplateImageRepository : JpaRepository<UITemplateImage, String> {
    fun findByUiTemplateIdOrderByDisplayOrderAsc(uiTemplateId: String): List<UITemplateImage>
    fun findByUiTemplateIdAndImageTypeOrderByDisplayOrderAsc(uiTemplateId: String, imageType: UITemplateImageType): List<UITemplateImage>
}
