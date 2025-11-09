package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.notice.NoticeCreateRequest
import com.spectrum.workfolio.proto.notice.NoticeGetResponse
import com.spectrum.workfolio.proto.notice.NoticeListResponse
import com.spectrum.workfolio.proto.notice.NoticeUpdateRequest
import com.spectrum.workfolio.services.NoticeService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/notices")
class NoticeController(
    private val noticeService: NoticeService,
) {

    @GetMapping
    fun getNotices(): NoticeListResponse {
        return noticeService.getNotices()
    }

    @GetMapping("/{id}")
    fun getNotice(
        @PathVariable id: String,
    ): NoticeGetResponse {
        return noticeService.getNotice(id)
    }

    @PostMapping
    fun createNotice(
        @RequestBody request: NoticeCreateRequest,
    ): SuccessResponse {
        noticeService.createNotice(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updateNotice(
        @RequestBody request: NoticeUpdateRequest,
    ): SuccessResponse {
        noticeService.updateNotice(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @DeleteMapping("/{id}")
    fun deleteNotice(
        @PathVariable id: String,
    ): SuccessResponse {
        noticeService.deleteNotice(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}

