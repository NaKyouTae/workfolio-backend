package com.spectrum.workfolio.domain.entity.uitemplate

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.enums.UITemplateType
import com.spectrum.workfolio.domain.enums.WorkerUITemplateStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(
    name = "worker_ui_templates",
    indexes = [
        Index(name = "idx_worker_ui_templates_worker_status", columnList = "worker_id, status"),
        Index(
            name = "idx_worker_ui_templates_worker_template_expired",
            columnList = "worker_id, template_id, expired_at",
        ),
        Index(name = "idx_worker_ui_templates_expired", columnList = "expired_at"),
    ],
)
class WorkerUITemplate(
    worker: Worker,
    uiTemplate: UITemplate,
    purchasedAt: LocalDateTime,
    expiredAt: LocalDateTime,
    creditsUsed: Int,
    templateType: UITemplateType,
    status: WorkerUITemplateStatus = WorkerUITemplateStatus.ACTIVE,
    isDefault: Boolean = false,
) : BaseEntity("WU") {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    var status: WorkerUITemplateStatus = status
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", length = 32, nullable = false)
    var templateType: UITemplateType = templateType
        protected set

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = isDefault
        protected set

    @Column(name = "deleted_at")
    var deletedAt: LocalDateTime? = null
        protected set

    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(expiredAt)
    }

    fun isValid(): Boolean {
        return status == WorkerUITemplateStatus.ACTIVE && !isExpired()
    }

    fun softDelete() {
        this.status = WorkerUITemplateStatus.DELETED
        this.deletedAt = LocalDateTime.now()
    }

    fun extendExpiration(additionalDays: Int) {
        this.expiredAt = this.expiredAt.plusDays(additionalDays.toLong())
    }

    fun reactivate(now: LocalDateTime, durationDays: Int, price: Int) {
        this.purchasedAt = now
        this.expiredAt = now.plusDays(durationDays.toLong())
        this.creditsUsed = price
    }

    fun markAsDefault() {
        this.isDefault = true
    }

    fun clearDefault() {
        this.isDefault = false
    }
}
