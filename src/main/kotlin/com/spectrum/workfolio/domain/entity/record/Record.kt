package com.spectrum.workfolio.domain.entity.record

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.model.RecordType
import jakarta.persistence.*
import java.time.Duration
import java.time.LocalDateTime

/**
 * 기록
 */
@Entity
@Table(
    name = "record",
    indexes = [
        Index(name = "IDX_RECORD_TITLE", columnList = "title"),
        Index(name = "IDX_RECORD_WORKER_ID", columnList = "worker_id")
    ]
)
class Record(
    title: String,
    description: String,
    type: RecordType,
    startedAt: LocalDateTime,
    endedAt: LocalDateTime? = null,
    recordGroup: RecordGroup,
    worker: Worker,
) : BaseEntity("RE") {
    @Column(name = "title", nullable = false)
    var title: String = title
        protected set

    @Column(name = "description", nullable = false)
    var description: String = description
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: RecordType = type
        protected set

    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDateTime = startedAt
        protected set

    @Column(name = "ended_at", nullable = true)
    var endedAt: LocalDateTime? = endedAt
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_group_id", nullable = false)
    var recordGroup: RecordGroup = recordGroup
        protected set

    fun getDuration(): Int {
        return Duration.between(startedAt, endedAt).toDays().toInt()
    }

    fun getPriority(): Int {
        Duration.between(startedAt, endedAt).toDays().toInt()

        return 0
    }

    companion object {
        fun generateRecordType(startedAt: LocalDateTime, endedAt: LocalDateTime): RecordType {
            val duration = Duration.between(startedAt, endedAt)

            return when {
                duration.toDays() >= 1 -> RecordType.MULTI_DAY
                duration.toHours() < 24 -> RecordType.TIME
                else -> RecordType.DAY
            }
        }
    }
}
