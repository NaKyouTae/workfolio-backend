package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.common.Notice
import com.spectrum.workfolio.domain.entity.common.NoticeRead
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.NoticeReadRepository
import com.spectrum.workfolio.domain.repository.NoticeRepository
import com.spectrum.workfolio.domain.repository.WorkerRepository
import com.spectrum.workfolio.proto.notice.NoticeCreateRequest
import com.spectrum.workfolio.proto.notice.NoticeGetResponse
import com.spectrum.workfolio.proto.notice.NoticeListResponse
import com.spectrum.workfolio.proto.notice.NoticeUnreadCountResponse
import com.spectrum.workfolio.proto.notice.NoticeUpdateRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NoticeService(
    private val noticeRepository: NoticeRepository,
    private val noticeReadRepository: NoticeReadRepository,
    private val workerRepository: WorkerRepository,
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

    @Transactional
    fun markAsRead(workerId: String, noticeId: String) {
        if (noticeReadRepository.existsByWorkerIdAndNoticeId(workerId, noticeId)) {
            return
        }

        val worker = workerRepository.findById(workerId)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_WORKER.message) }
        val notice = getNoticeById(noticeId)

        noticeReadRepository.save(NoticeRead(worker = worker, notice = notice))
    }

    @Transactional(readOnly = true)
    fun getUnreadCount(workerId: String): NoticeUnreadCountResponse {
        val totalCount = noticeRepository.count()
        val readNoticeIds = noticeReadRepository.findReadNoticeIdsByWorkerId(workerId)
        val unreadCount = totalCount - readNoticeIds.size

        return NoticeUnreadCountResponse.newBuilder()
            .setUnreadCount(maxOf(unreadCount.toInt(), 0))
            .build()
    }
}
