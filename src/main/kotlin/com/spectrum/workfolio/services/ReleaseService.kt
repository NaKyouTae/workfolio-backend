package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.NoticeRepository
import com.spectrum.workfolio.proto.release.ReleaseNoticeListResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReleaseService(
    private val noticeRepository: NoticeRepository,
) {

    @Transactional(readOnly = true)
    fun getNotices(): ReleaseNoticeListResponse {
        val notices = noticeRepository.findAllByOrderByIsPinnedDescCreatedAtDesc()
        val noticeProtos = notices.map { it.toProto() }
        return ReleaseNoticeListResponse.newBuilder().addAllNotices(noticeProtos).build()
    }
}
