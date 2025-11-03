package com.spectrum.workfolio.domain.entity.record

import com.spectrum.workfolio.domain.entity.BaseAttachment
import jakarta.persistence.Entity
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
    name = "record_attachments",
    indexes = [
        Index(name = "idx_record_attachments_record_id_created_at", columnList = "record_id, created_at"),
    ],
)
class RecordAttachment(
    fileName: String,
    fileUrl: String,
    record: Record,
) : BaseAttachment(
    prefixId = "DA",
    fileName = fileName,
    fileUrl = fileUrl,
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = false)
    var record: Record = record
        protected set

    fun changeInfo(
        fileName: String,
        fileUrl: String,
    ) {
        this.fileName = fileName
        this.fileUrl = fileUrl
    }
}
