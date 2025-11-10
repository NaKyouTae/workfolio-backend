package com.spectrum.workfolio.domain.entity.plan

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.enums.Currency
import com.spectrum.workfolio.domain.enums.PlanType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(
    name = "plans",
    indexes = [
        Index(name = "idx_plans_type", columnList = "type"),
        Index(name = "idx_plans_priority", columnList = "priority"),
    ],
)
class Plan(
    name: String,
    type: PlanType,
    price: BigDecimal,
    currency: Currency,
    priority: Int = 0,
    description: String? = null,
) : BaseEntity("PL") {
    @Column(name = "name", length = 128, nullable = false)
    var name: String = name
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 64, nullable = false)
    var type: PlanType = type
        protected set

    @Column(name = "price", nullable = false)
    var price: BigDecimal = price
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", length = 8, nullable = false)
    var currency: Currency = currency
        protected set

    @Column(name = "priority", nullable = false)
    var priority: Int = priority
        protected set

    @Column(name = "description", columnDefinition = "TEXT", nullable = true)
    var description: String? = description
        protected set

    @OneToMany(mappedBy = "plan", fetch = FetchType.LAZY)
    private var mutablePlanFeatures: MutableList<PlanFeature> = mutableListOf()
    val planFeatures: List<PlanFeature> get() = mutablePlanFeatures.toList()

    fun changeInfo(
        name: String,
        price: BigDecimal,
        currency: Currency,
        priority: Int,
        description: String?,
    ) {
        this.name = name
        this.price = price
        this.currency = currency
        this.priority = priority
        this.description = description
    }
}
