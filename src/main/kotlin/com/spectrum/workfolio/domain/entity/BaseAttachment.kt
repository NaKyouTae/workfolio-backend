package com.spectrum.workfolio.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
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
