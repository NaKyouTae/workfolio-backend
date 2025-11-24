package com.spectrum.workfolio.domain.entity.record

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.enums.RecordGroupRole
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

/**
 * 기록 그룹
 */
@Entity
@Table(
    name = "worker_record_groups",
    indexes = [
        Index(name = "idx_worker_record_groups_public_id", columnList = "public_id"),
        Index(name = "idx_worker_record_groups_worker_id", columnList = "worker_id"),
        Index(name = "idx_worker_record_groups_record_group_id", columnList = "record_group_id"),
        Index(name = "idx_worker_record_groups_worker_id_record_group_id", columnList = "worker_id, record_group_id"),
    ],
)
class WorkerRecordGroup(
    publicId: String,
    worker: Worker,
    recordGroup: RecordGroup,
    role: RecordGroupRole,
    priority: Long = 0,
) : BaseEntity("WR") {

    @Column(name = "public_id", length = 16, nullable = false)
    var publicId: String = publicId
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_group_id", nullable = false)
    var recordGroup: RecordGroup = recordGroup
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 32, nullable = false)
    var role: RecordGroupRole = role
        protected set

    @Column(name = "priority", nullable = false)
    var priority: Long = priority
        protected set

    fun changeRole(role: RecordGroupRole) {
        this.role = role
    }

    fun changePriority(priority: Long) {
        this.priority = priority
    }
}
