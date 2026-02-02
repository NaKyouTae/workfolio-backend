package com.spectrum.workfolio.domain.entity.uitemplate

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "worker_ui_templates",
    indexes = [
        Index(name = "idx_worker_ui_templates_worker_active", columnList = "worker_id, is_active"),
        Index(name = "idx_worker_ui_templates_worker_template_expired", columnList = "worker_id, ui_template_id, expired_at"),
        Index(name = "idx_worker_ui_templates_expired", columnList = "expired_at"),
    ],
)
class WorkerUITemplate(
    worker: Worker,
    uiTemplate: UITemplate,
    purchasedAt: LocalDateTime,
    expiredAt: LocalDateTime,
    creditsUsed: Int,
    isActive: Boolean = true,
) : BaseEntity("WU") {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ui_template_id", nullable = false)
    var uiTemplate: UITemplate = uiTemplate
        protected set

    @Column(name = "purchased_at", nullable = false)
    var purchasedAt: LocalDateTime = purchasedAt
        protected set

    @Column(name = "expired_at", nullable = false)
    var expiredAt: LocalDateTime = expiredAt
        protected set

    @Column(name = "credits_used", nullable = false)
    var creditsUsed: Int = creditsUsed
        protected set

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = isActive
        protected set

    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiredAt)
    }

    fun isValid(): Boolean {
        return isActive && !isExpired()
    }

    fun deactivate() {
        this.isActive = false
    }

    fun extendExpiration(additionalDays: Int) {
        this.expiredAt = this.expiredAt.plusDays(additionalDays.toLong())
    }
}
