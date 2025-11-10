package com.spectrum.workfolio.domain.entity.plan

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "worker_feature_usages",
    indexes = [
        Index(name = "idx_worker_feature_usage_worker_id", columnList = "worker_id"),
    ],
)
class WorkerFeatureUsage(
    currentCount: Int,
    limitCount: Int? = null,
    lastUsedAt: LocalDateTime,
    feature: Feature,
    worker: Worker,
) : BaseEntity("WF") {

    @Column(name = "current_count", nullable = false)
    var currentCount: Int = currentCount
        protected set

    @Column(name = "limit_count", nullable = true)
    var limitCount: Int? = limitCount
        protected set

    @Column(name = "last_used_at", nullable = false)
    var lastUsedAt: LocalDateTime = lastUsedAt
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    var feature: Feature = feature
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set

    fun changeInfo(
        currentCount: Int,
        limitCount: Int,
        lastUsedAt: LocalDateTime,
    ) {
        this.currentCount = currentCount
        this.limitCount = limitCount
        this.lastUsedAt = lastUsedAt
    }
}
