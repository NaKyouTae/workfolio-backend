package com.spectrum.workfolio.domain.entity.uitemplate

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "ui_template_plans",
    indexes = [
        Index(name = "idx_ui_template_plans_template_order", columnList = "ui_template_id, display_order"),
    ],
)
class UiTemplatePlan(
    uiTemplate: UITemplate,
    durationDays: Int,
    price: Int,
    displayOrder: Int = 0,
) : BaseEntity("UP") {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ui_template_id", nullable = false)
    var uiTemplate: UITemplate = uiTemplate
        protected set

    @Column(name = "duration_days", nullable = false)
    var durationDays: Int = durationDays
        protected set

    @Column(name = "price", nullable = false)
    var price: Int = price
        protected set

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = displayOrder
        protected set
}
