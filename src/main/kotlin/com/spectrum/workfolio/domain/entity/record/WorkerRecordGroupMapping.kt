package com.spectrum.workfolio.domain.entity.record

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import jakarta.persistence.*

/**
 * 기록 그룹
 */
@Entity
@Table(
    name = "worker_record_group_mapping",
    indexes = [
        Index(name = "IDX_RECORD_GROUP_PUBLIC_ID", columnList = "public_id"),
    ]
)
class WorkerRecordGroupMapping(
    publicId: String,
    worker: Worker,
    recordGroup: RecordGroup,
) : BaseEntity("WR") {

    @Column(name = "public_id", nullable = false)
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
