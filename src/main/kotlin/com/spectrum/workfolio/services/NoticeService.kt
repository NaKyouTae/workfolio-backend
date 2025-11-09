package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.common.Notice
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.NoticeRepository
import com.spectrum.workfolio.proto.notice.NoticeCreateRequest
import com.spectrum.workfolio.proto.notice.NoticeGetResponse
import com.spectrum.workfolio.proto.notice.NoticeListResponse
import com.spectrum.workfolio.proto.notice.NoticeUpdateRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NoticeService(
    private val noticeRepository: NoticeRepository,
) {

    fun getNoticeById(id: String): Notice {
        return noticeRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_NOTICE.message) }
    }

    @Transactional(readOnly = true)
    fun getNotices(): NoticeListResponse {
        val notices = noticeRepository.findAllByOrderByIsPinnedDescCreatedAtDesc()
        val noticeProtos = notices.map { it.toProto() }
        return NoticeListResponse.newBuilder().addAllNotices(noticeProtos).build()
    }

    @Transactional(readOnly = true)
    fun getNotice(id: String): NoticeGetResponse {
        val notice = getNoticeById(id)
        return NoticeGetResponse.newBuilder().setNotice(notice.toProto()).build()
    }

    @Transactional
    fun createNotice(request: NoticeCreateRequest) {
        val notice = Notice(
            title = request.title,
            content = request.content,
            isPinned = request.isPinned,
        )

        noticeRepository.save(notice)
    }

    @Transactional
    fun updateNotice(request: NoticeUpdateRequest) {
        val notice = getNoticeById(request.id)

        notice.changeInfo(
            title = request.title,
            content = request.content,
            isPinned = request.isPinned,
        )

        noticeRepository.save(notice)
    }

    @Transactional
    fun deleteNotice(id: String) {
        val notice = getNoticeById(id)
        noticeRepository.delete(notice)
    }
}

