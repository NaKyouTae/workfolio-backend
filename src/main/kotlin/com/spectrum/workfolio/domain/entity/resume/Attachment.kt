package com.spectrum.workfolio.domain.entity.resume

import com.spectrum.workfolio.domain.entity.BaseEntity
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
 * 첨부파일
 */
@Entity
@Table(
    name = "attachments",
    indexes = [
        Index(name = "idx_attachments_resume_id", columnList = "resume_id"),
    ],
)
class Attachment(
    fileName: String,
    fileUrl: String,
    isVisible: Boolean,
    type: AttachmentType? = null,
    resume: Resume,
) : BaseEntity("AH") {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 512, nullable = true)
    var type: AttachmentType? = type
        protected set

    @Column(name = "file_name", length = 1024, nullable = false)
    var fileName: String = fileName
        protected set

    @Column(name = "file_url", columnDefinition = "TEXT", nullable = false)
    var fileUrl: String = fileUrl
        protected set

    @Column(name = "is_visible", nullable = false)
    var isVisible: Boolean = isVisible
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    var resume: Resume = resume
        protected set

    fun changeInfo(
        fileName: String,
        fileUrl: String,
        isVisible: Boolean,
        type: AttachmentType? = null,
    ) {
        this.type = type
        this.fileName = fileName
        this.fileUrl = fileUrl
        this.isVisible = isVisible
    }
}
