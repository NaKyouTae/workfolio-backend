package com.spectrum.workfolio.domain.dto

import com.google.protobuf.ByteString
import com.spectrum.workfolio.domain.enums.AttachmentCategory
import com.spectrum.workfolio.domain.enums.AttachmentType

data class AttachmentUpdateDto(
    val id: String,
    val storagePath: String,
    val type: AttachmentType,
    val category: AttachmentCategory,

    val isVisible: Boolean = false,
    val priority: Int = 0,
    val fileName: String? = null,
    val fileUrl: String? = null,
    val url: String? = null,
    val fileData: ByteString? = null,
)
