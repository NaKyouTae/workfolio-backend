package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.extensions.toReleaseDetailProto
import com.spectrum.workfolio.domain.repository.NoticeRepository
import com.spectrum.workfolio.domain.repository.PlanRepository
import com.spectrum.workfolio.domain.repository.PlanSubscriptionRepository
import com.spectrum.workfolio.proto.release.ReleaseNoticeListResponse
import com.spectrum.workfolio.proto.release.ReleasePlanListResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReleaseService(
    private val noticeRepository: NoticeRepository,
    private val planRepository: PlanRepository,
    private val planSubscriptionRepository: PlanSubscriptionRepository,
) {

    @Transactional(readOnly = true)
    fun getNotices(): ReleaseNoticeListResponse {
        val notices = noticeRepository.findAllByOrderByIsPinnedDescCreatedAtDesc()
        val noticeProtos = notices.map { it.toProto() }
        return ReleaseNoticeListResponse.newBuilder().addAllNotices(noticeProtos).build()
    }

    @Transactional(readOnly = true)
    fun getPlans(): ReleasePlanListResponse {
        val plans = planRepository.findAllByOrderByPriorityAsc()
        
        val planDetails = plans.map { plan ->
            val planSubscriptions = planSubscriptionRepository.findByPlanIdOrderByPriorityAsc(plan.id)
            plan.toReleaseDetailProto(planSubscriptions)
        }
        
        return ReleasePlanListResponse.newBuilder().addAllPlans(planDetails).build()
    }
}
