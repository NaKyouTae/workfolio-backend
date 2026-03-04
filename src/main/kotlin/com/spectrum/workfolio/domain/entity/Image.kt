package com.spectrum.workfolio.domain.entity

import com.spectrum.workfolio.domain.enums.ImageExtType
import com.spectrum.workfolio.domain.enums.ImageStatus
import com.spectrum.workfolio.domain.enums.ImageTargetType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "images",
    indexes = [
        Index(name = "idx_images_target", columnList = "target_type, target_id, priority"),
    ],
)
class Image(
    status: ImageStatus = ImageStatus.ACTIVE,
    targetType: ImageTargetType,
    targetId: String,
    extType: ImageExtType,
    url: String,
    priority: Int = 0,
) : BaseEntity("IM") {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    var status: ImageStatus = status
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 32, nullable = false)
    var targetType: ImageTargetType = targetType
        protected set

    @Column(name = "target_id", length = 28, nullable = false)
    var targetId: String = targetId
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "ext_type", length = 32, nullable = false)
    var extType: ImageExtType = extType
        protected set

    @Column(name = "url", columnDefinition = "TEXT", nullable = false)
    var url: String = url
        protected set

    @Column(name = "priority", nullable = false)
    var priority: Int = priority
        protected set

    fun changeInfo(
        extType: ImageExtType,
        url: String,
        priority: Int,
    ) {
        this.extType = extType
        this.url = url
        this.priority = priority
    }

    fun softDelete() {
        this.status = ImageStatus.DELETED
    }
}
