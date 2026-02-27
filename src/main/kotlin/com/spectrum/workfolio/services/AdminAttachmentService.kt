package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.common.Attachment
import com.spectrum.workfolio.domain.repository.AttachmentRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminAttachmentService(
    private val attachmentRepository: AttachmentRepository,
    private val supabaseStorageService: SupabaseStorageService,
) {
    @Transactional(readOnly = true)
    fun getAttachments(page: Int, size: Int): AdminAttachmentListResponse {
        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceIn(1, 200)
        val pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))
        val attachmentPage = attachmentRepository.findAll(pageable)

        val items = attachmentPage.content.map { attachment ->
            attachment.toAdminResponse(supabaseStorageService.getFileSizeByUrl(attachment.fileUrl))
        }

        return AdminAttachmentListResponse(
            attachments = items,
            totalElements = attachmentPage.totalElements,
            totalPages = attachmentPage.totalPages,
            currentPage = safePage,
        )
    }

    private fun Attachment.toAdminResponse(fileSizeBytes: Long): AdminAttachmentResponse {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return AdminAttachmentResponse(
            id = id,
            fileName = fileName,
            fileUrl = fileUrl,
            targetId = targetId,
            targetType = targetType.name,
            category = category.name,
            type = type.name,
            fileExtension = extension,
            fileSizeBytes = fileSizeBytes,
            createdAt = createdAt.toString(),
            updatedAt = updatedAt.toString(),
        )
    }
}

data class AdminAttachmentListResponse(
    val attachments: List<AdminAttachmentResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
)

data class AdminAttachmentResponse(
    val id: String,
    val fileName: String,
    val fileUrl: String,
    val targetId: String,
    val targetType: String,
    val category: String,
    val type: String,
    val fileExtension: String,
    val fileSizeBytes: Long,
    val createdAt: String,
    val updatedAt: String,
)
