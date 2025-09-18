package com.spectrum.workfolio.domain.entity.record

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import jakarta.persistence.Column
import jakarta.persistence.Entity
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
    name = "worker_record_group",
    indexes = [
        Index(name = "idx_record_group_public_id", columnList = "public_id"),
        Index(name = "idx_record_group_worker_id", columnList = "worker_id"),
        Index(name = "idx_record_group_record_group_id", columnList = "record_group_id"),
        Index(name = "idx_record_group_worker_id_record_group_id", columnList = "worker_id, record_group_id"),
    ],
)
class WorkerRecordGroup(
    publicId: String,
    worker: Worker,
    recordGroup: RecordGroup,
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
}
