package com.spectrum.workfolio.domain.dto

import com.google.protobuf.ByteString
import com.spectrum.workfolio.domain.enums.AttachmentCategory
import com.spectrum.workfolio.domain.enums.AttachmentType

data class AttachmentCreateDto(
    val targetId: String,
    val storagePath: String,

    val category: AttachmentCategory? = null,
    val isVisible: Boolean = false,
    val priority: Int = 0,
    val url: String? = null,
    val type: AttachmentType? = null,
    val fileName: String? = null,
    val fileUrl: String? = null,
    val fileData: ByteString? = null,
)
