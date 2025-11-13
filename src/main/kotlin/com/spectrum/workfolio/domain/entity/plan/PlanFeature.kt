package com.spectrum.workfolio.domain.entity.plan

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "plan_features",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_plan_features_plan_feature",
            columnNames = ["plan_id", "feature_id"],
        ),
    ],
    indexes = [
        Index(name = "idx_plan_features_plan_id", columnList = "plan_id"),
        Index(name = "idx_plan_features_feature_id", columnList = "feature_id"),
    ],
)
class PlanFeature(
    plan: Plan,
    feature: Feature,
    limitCount: Int? = null,
    description: String,
) : BaseEntity("PF") {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    var plan: Plan = plan
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feature_id", nullable = false)
    var feature: Feature = feature
        protected set

    @Column(name = "limit_count", nullable = true)
    var limitCount: Int? = limitCount
        protected set

    @Column(name = "description", columnDefinition = "TEXT", nullable = true)
    var description: String? = description
        protected set

    fun changeInfo(
        plan: Plan,
        feature: Feature,
        limitCount: Int?,
        description: String,
    ) {
        this.plan = plan
        this.feature = feature
        this.limitCount = limitCount
        this.description = description
    }
}
