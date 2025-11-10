package com.spectrum.workfolio.domain.entity.plan

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(
    name = "plan_subscriptions",
    indexes = [
        Index(name = "idx_plan_subscriptions_priority", columnList = "priority"),
    ],
)
class PlanSubscription(
    durationMonths: Int,
    totalPrice: BigDecimal,
    monthlyEquivalent: BigDecimal,
    savingsAmount: BigDecimal,
    discountRate: Int,
    priority: Int = 0,
    plan: Plan,
) : BaseEntity("PO") {
    @Column(name = "duration_months", nullable = false)
    var durationMonths: Int = durationMonths
        protected set

    @Column(name = "total_price", nullable = false)
    var totalPrice: BigDecimal = totalPrice
        protected set

    @Column(name = "monthly_equivalent", nullable = false)
    var monthlyEquivalent: BigDecimal = monthlyEquivalent
        protected set

    @Column(name = "savings_amount", nullable = false)
    var savingsAmount: BigDecimal = savingsAmount
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
        totalPrice: BigDecimal,
        monthlyEquivalent: BigDecimal,
        savingsAmount: BigDecimal,
        discountRate: Int,
        priority: Int = 0,
    ) {
        this.durationMonths = durationMonths
        this.totalPrice = totalPrice
        this.discountRate = discountRate
        this.monthlyEquivalent = monthlyEquivalent
        this.savingsAmount = savingsAmount
        this.priority = priority
    }
}
