package com.spectrum.workfolio.domain.entity.common

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "notice_reads",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_notice_reads_worker_notice", columnNames = ["worker_id", "notice_id"]),
    ],
    indexes = [
        Index(name = "idx_notice_reads_worker_id", columnList = "worker_id"),
    ],
)
class NoticeRead(
    worker: Worker,
    notice: Notice,
) : BaseEntity("NR") {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    val worker: Worker = worker

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    val notice: Notice = notice
}
