package com.spectrum.workfolio.domain.entity.resume

import com.spectrum.workfolio.domain.entity.BaseAttachment
import com.spectrum.workfolio.domain.enums.AttachmentCategory
import com.spectrum.workfolio.domain.enums.AttachmentType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

/**
 * 이력서 첨부파일
 */
@Entity
@Table(
    name = "resume_attachments",
    indexes = [
        Index(name = "idx_resume_attachments_resume_id_priority", columnList = "resume_id, priority"),
    ],
)
class ResumeAttachment(
    fileName: String,
    fileUrl: String,
    url: String,
    isVisible: Boolean,
    priority: Int = 0,
    category: AttachmentCategory,
    type: AttachmentType? = null,
    resume: Resume,
) : BaseAttachment(
    prefixId = "EA",
    fileName = fileName,
    fileUrl = fileUrl,
) {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 512, nullable = true)
    var type: AttachmentType? = type
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    var resume: Resume = resume
        protected set

    fun changeInfo(
        fileName: String,
        fileUrl: String,
        url: String,
        isVisible: Boolean,
        priority: Int = 0,
        category: AttachmentCategory,
        type: AttachmentType? = null,
    ) {
        this.type = type
        this.category = category
        this.fileName = fileName
        this.fileUrl = fileUrl
        this.url = url
        this.isVisible = isVisible
        this.priority = priority
    }
}
