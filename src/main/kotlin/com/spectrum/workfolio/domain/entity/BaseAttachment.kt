package com.spectrum.workfolio.domain.entity

import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.AttachmentCategory
import com.spectrum.workfolio.domain.enums.AttachmentType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Table
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseAttachment(
    prefixId: String,
    fileName: String,
    fileUrl: String,
) : BaseEntity(prefixId) {

    @Column(name = "file_name", length = 1024, nullable = false)
    var fileName: String = fileName
        protected set

    @Column(name = "file_url", columnDefinition = "TEXT", nullable = false)
    var fileUrl: String = fileUrl
        protected set

    fun changeFileUrl(
        fileUrl: String,
    ) {
        this.fileUrl = fileUrl
    }
}