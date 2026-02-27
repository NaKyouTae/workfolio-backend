package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.services.AdminAttachmentListResponse
import com.spectrum.workfolio.services.AdminAttachmentService
import com.spectrum.workfolio.services.AttachmentCommandService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/attachments")
class AdminAttachmentController(
    private val adminAttachmentService: AdminAttachmentService,
    private val attachmentCommandService: AttachmentCommandService,
) {
    @GetMapping
    fun getAttachments(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): AdminAttachmentListResponse {
        return adminAttachmentService.getAttachments(page, size)
    }

    @DeleteMapping("/{id}")
    fun deleteAttachment(@PathVariable id: String): SuccessResponse {
        attachmentCommandService.deleteAttachments(listOf(id))
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
