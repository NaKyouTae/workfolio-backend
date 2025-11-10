package com.spectrum.workfolio.domain.entity.plan

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.enums.WorkerSubscriptionStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime

@Entity
@Table(
    name = "worker_subscriptions",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_worker_subscriptions_plan_option_feature",
            columnNames = ["plan_option_id", "feature_id"],
        ),
    ],
    indexes = [
        Index(name = "idx_worker_subscriptions_worker_id", columnList = "worker_id"),
        Index(name = "idx_worker_subscriptions_status", columnList = "status"),
        Index(name = "idx_worker_subscriptions_worker_status", columnList = "worker_id, status"),
    ],
)
class WorkerSubscription(
    status: WorkerSubscriptionStatus,
    startedAt: LocalDateTime,
    endedAt: LocalDateTime,
    cancelledAt: LocalDateTime? = null,
    cancelReason: String? = null,
    planSubscription: PlanSubscription,
    worker: Worker,
) : BaseEntity("WS") {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    var status: WorkerSubscriptionStatus = status
        protected set

    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDateTime = startedAt
        protected set

    @Column(name = "ended_at", nullable = false)
    var endedAt: LocalDateTime = endedAt
        protected set

    @Column(name = "cancelled_at", nullable = true)
    var cancelledAt: LocalDateTime? = cancelledAt
        protected set

    @Column(name = "cancel_reason", length = 512, nullable = true)
    var cancelReason: String? = cancelReason
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_subscription_id", nullable = false)
    var planSubscription: PlanSubscription = planSubscription
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set

    fun isActive(): Boolean {
        return status == WorkerSubscriptionStatus.ACTIVE &&
            LocalDateTime.now().isBefore(endedAt) &&
            cancelledAt == null
    }

    fun cancel(reason: String) {
        this.status = WorkerSubscriptionStatus.CANCELLED
        this.cancelledAt = LocalDateTime.now()
        this.cancelReason = reason
    }

    fun changeInfo(
        startedAt: LocalDateTime,
        endedAt: LocalDateTime,
        cancelledAt: LocalDateTime? = null,
        cancelReason: String? = null,
    ) {
        this.startedAt = startedAt
        this.endedAt = endedAt
        this.cancelledAt = cancelledAt
        this.cancelReason = cancelReason
    }
}
