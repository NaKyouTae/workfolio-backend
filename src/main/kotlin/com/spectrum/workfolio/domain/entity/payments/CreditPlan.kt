package com.spectrum.workfolio.domain.entity.payments

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "credit_plans",
    indexes = [
        Index(name = "idx_credit_plans_active_order", columnList = "is_active, display_order"),
    ],
)
class CreditPlan(
    name: String,
    description: String? = null,
    price: Int,
    baseCredits: Int,
    bonusCredits: Int = 0,
    isActive: Boolean = true,
    displayOrder: Int = 0,
    isPopular: Boolean = false,
) : BaseEntity("CP") {

    @Column(name = "name", length = 100, nullable = false)
    var name: String = name
        protected set

    @Column(name = "description", columnDefinition = "TEXT", nullable = true)
    var description: String? = description
        protected set

    @Column(name = "price", nullable = false)
    var price: Int = price
        protected set

    @Column(name = "base_credits", nullable = false)
    var baseCredits: Int = baseCredits
        protected set

    @Column(name = "bonus_credits", nullable = false)
    var bonusCredits: Int = bonusCredits
        protected set

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = isActive
        protected set

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = displayOrder
        protected set

    @Column(name = "is_popular", nullable = false)
    var isPopular: Boolean = isPopular
        protected set

    val totalCredits: Int get() = baseCredits + bonusCredits

    fun changeInfo(
        name: String,
        description: String?,
        price: Int,
        baseCredits: Int,
        bonusCredits: Int,
        isActive: Boolean,
        displayOrder: Int,
        isPopular: Boolean,
    ) {
        this.name = name
        this.description = description
        this.price = price
        this.baseCredits = baseCredits
        this.bonusCredits = bonusCredits
        this.isActive = isActive
        this.displayOrder = displayOrder
        this.isPopular = isPopular
    }

    fun deactivate() {
        this.isActive = false
    }

    fun activate() {
        this.isActive = true
    }
}
