package com.spectrum.workfolio.domain.entity.common

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.enums.AttachmentCategory
import com.spectrum.workfolio.domain.enums.AttachmentTargetType
import com.spectrum.workfolio.domain.enums.AttachmentType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table

/**
 * 이력서 첨부파일
 */
@Entity
@Table(
    name = "attachments",
    indexes = [
        Index(name = "idx_attachments_target_id_priority", columnList = "target_id, priority"),
    ],
)
class Attachment(
    fileName: String,
    fileUrl: String,
    url: String,
    isVisible: Boolean,
    priority: Int = 0,
    category: AttachmentCategory,
    type: AttachmentType,
    targetId: String,
    targetType: AttachmentTargetType,
) : BaseEntity("AT") {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 512, nullable = false)
    var type: AttachmentType = type
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 512, nullable = false)
    var category: AttachmentCategory = category
        protected set

    @Column(name = "url", columnDefinition = "TEXT", nullable = false)
    var url: String = url
        protected set

    @Column(name = "is_visible", nullable = false)
    var isVisible: Boolean = isVisible
        protected set

    @Column(name = "priority", nullable = false)
    var priority: Int = priority
        protected set

    @Column(name = "file_name", length = 1024, nullable = false)
    var fileName: String = fileName
        protected set

    @Column(name = "file_url", columnDefinition = "TEXT", nullable = false)
    var fileUrl: String = fileUrl
        protected set

    @Column(name = "target_id", length = 16, nullable = false)
    var targetId: String = targetId
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 32, nullable = false)
    var targetType: AttachmentTargetType = targetType
        protected set

    fun changeInfo(
        fileName: String,
        fileUrl: String,
        url: String,
        isVisible: Boolean,
        priority: Int = 0,
        category: AttachmentCategory,
        type: AttachmentType,
    ) {
        this.type = type
        this.category = category
        this.fileName = fileName
        this.fileUrl = fileUrl
        this.url = url
        this.isVisible = isVisible
        this.priority = priority
    }

    fun changeFileUrl(
        fileUrl: String,
    ) {
        this.fileUrl = fileUrl
    }
}
