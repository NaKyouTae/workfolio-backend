package com.spectrum.workfolio.domain.entity.record

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.utils.StringUtil
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
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
    name = "record_group",
    indexes = [
        Index(name = "idx_record_group_public_id", columnList = "public_id"),
    ],
)
class RecordGroup(
    title: String,
    color: String,
    isPublic: Boolean,
    publicId: String,
    priority: Long,
    worker: Worker,
) : BaseEntity("RG") {
    @Column(name = "title", nullable = false)
    var title: String = title
        protected set

    @Column(name = "color", nullable = false)
    var color: String = color // hex code
        protected set

    @Column(name = "is_public", nullable = false)
    var isPublic: Boolean = isPublic
        protected set

    @Column(name = "public_id", nullable = false)
    var publicId: String = publicId
        protected set

    @Column(name = "priority", nullable = false)
    var priority: Long = priority
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set

    @OneToMany(mappedBy = "recordGroup", cascade = [CascadeType.REMOVE])
    private var mutableRecords: MutableList<Record> = mutableListOf()
    val records: List<Record> get() = mutableRecords.toList()

    fun changeRecordGroup(
        title: String,
        color: String,
        isPublic: Boolean,
        priority: Long,
    ) {
        this.title = title
        this.color = color
        this.isPublic = isPublic
        this.priority = priority
    }

    companion object {
        fun generateShortPublicId(): String {
            val uuid = StringUtil.generateRandomString(16)
            return "WORKFOLIO:$uuid"
        }
    }
}
