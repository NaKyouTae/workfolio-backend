package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.Image
import com.spectrum.workfolio.domain.enums.ImageExtType
import com.spectrum.workfolio.domain.enums.ImageStatus
import com.spectrum.workfolio.domain.enums.ImageTargetType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ImageRepository : JpaRepository<Image, String> {
    fun findByTargetTypeAndTargetIdAndStatusOrderByPriorityAsc(
        targetType: ImageTargetType,
        targetId: String,
        status: ImageStatus = ImageStatus.ACTIVE,
    ): List<Image>

    fun findByTargetTypeAndTargetIdAndExtTypeAndStatusOrderByPriorityAsc(
        targetType: ImageTargetType,
        targetId: String,
        extType: ImageExtType,
        status: ImageStatus = ImageStatus.ACTIVE,
    ): List<Image>

    fun findByTargetTypeAndTargetIdInAndStatusOrderByPriorityAsc(
        targetType: ImageTargetType,
        targetIds: List<String>,
        status: ImageStatus = ImageStatus.ACTIVE,
    ): List<Image>

    fun deleteByTargetTypeAndTargetId(targetType: ImageTargetType, targetId: String)
}
