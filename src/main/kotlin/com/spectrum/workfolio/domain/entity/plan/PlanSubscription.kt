package com.spectrum.workfolio.domain.entity.plan

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(
    name = "plan_subscriptions",
    indexes = [
        Index(name = "idx_plan_subscriptions_priority", columnList = "priority"),
        Index(name = "idx_plan_subscriptions_plan_id", columnList = "plan_id"),
    ],
)
class PlanSubscription(
    durationMonths: Int,
    totalPrice: Long,
    monthlyEquivalent: Long,
    savingsAmount: Long,
    discountRate: Int,
    priority: Int = 0,
    plan: Plan,
) : BaseEntity("PS") {
    @Column(name = "duration_months", nullable = false)
    var durationMonths: Int = durationMonths
        protected set

    @Column(name = "total_price", nullable = false)
    var totalPrice: Long = totalPrice
        protected set

    @Column(name = "monthly_equivalent", nullable = false)
    var monthlyEquivalent: Long = monthlyEquivalent
        protected set

    @Column(name = "savings_amount", nullable = false)
    var savingsAmount: Long = savingsAmount
        protected set

    @Column(name = "discount_rate", nullable = false)
    var discountRate: Int = discountRate
        protected set

    @Column(name = "priority", nullable = false)
    var priority: Int = priority
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    var plan: Plan = plan
        protected set

    fun changeInfo(
        durationMonths: Int,
        totalPrice: Long,
        monthlyEquivalent: Long,
        savingsAmount: Long,
        discountRate: Int,
        priority: Int,
        plan: Plan,
    ) {
        this.durationMonths = durationMonths
        this.totalPrice = totalPrice
        this.monthlyEquivalent = monthlyEquivalent
        this.savingsAmount = savingsAmount
        this.discountRate = discountRate
        this.priority = priority
        this.plan = plan
    }
}
