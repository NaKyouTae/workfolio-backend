package com.spectrum.workfolio.domain.entity.record

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import jakarta.persistence.*
import java.math.BigInteger
import java.time.LocalDateTime
import java.util.*

/**
 * 기록 그룹
 */
@Entity
@Table(
    name = "record_group",
    indexes = [
        Index(name = "IDX_RECORD_GROUP_PUBLIC_ID", columnList = "public_id"),
    ]
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

    companion object {
        fun generateShortPublicId(): String {
            val uuid = UUID.randomUUID().toString()
            val number = uuid.replace("-", "").substring(0, 16).uppercase()
            return "WORKFOLIO:$number"
        }
    }
}
