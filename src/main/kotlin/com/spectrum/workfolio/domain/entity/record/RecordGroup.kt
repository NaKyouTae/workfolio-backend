package com.spectrum.workfolio.domain.entity.record

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.enums.RecordGroupRole
import com.spectrum.workfolio.domain.enums.RecordGroupType
import com.spectrum.workfolio.utils.StringUtil
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

/**
 * 기록 그룹
 */
@Entity
@Table(
    name = "record_groups",
    indexes = [
        Index(name = "idx_record_groups_public_id", columnList = "public_id"),
        Index(name = "idx_record_groups_worker_id", columnList = "worker_id"),
    ],
)
class RecordGroup(
    title: String,
    color: String,
    isDefault: Boolean,
    publicId: String,
    type: RecordGroupType,
    role: RecordGroupRole,
    priority: Long,
    worker: Worker,
) : BaseEntity("RG") {
    @Column(name = "title", nullable = false)
    var title: String = title
        protected set

    @Column(name = "color", nullable = false)
    var color: String = color // hex code
        protected set

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = isDefault
        protected set

    @Column(name = "public_id", nullable = false)
    var publicId: String = publicId
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: RecordGroupType = type
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    var role: RecordGroupRole = role
        protected set

    @Column(name = "priority", nullable = false)
    var priority: Long = priority
        protected set

    // 소유주
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set

    @OneToMany(mappedBy = "recordGroup", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    private var mutableRecords: MutableList<Record> = mutableListOf()
    val records: List<Record> get() = mutableRecords.toList()

    @OneToMany(mappedBy = "recordGroup", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    private var mutableWorkerRecordGroups: MutableList<WorkerRecordGroup> = mutableListOf()
    val workerRecordGroups: List<WorkerRecordGroup> get() = mutableWorkerRecordGroups.toList()

    fun changeRecordGroup(
        title: String,
        color: String,
        priority: Long,
    ) {
        this.title = title
        this.color = color
        this.priority = priority
    }

    fun changeType(
        title: String,
        color: String,
        role: RecordGroupRole,
        type: RecordGroupType
    ) {
        this.title = title
        this.color = color
        this.role = role
        this.type = type
    }

    fun changeWorker(
        worker: Worker,
    ) {
        this.worker = worker
    }

    companion object {
        fun generateShortPublicId(): String {
            return StringUtil.generateRandomString(16)
        }
    }
}
