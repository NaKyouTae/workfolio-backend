package com.spectrum.workfolio.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

/**
 * 직장인
 */
@Entity
@Table(
    name = "worker",
    indexes = [
        Index(name = "IDX_WORKER_NAME", columnList = "name"),
    ]
)
class Worker(
    name: String,
): BaseEntity("WK") {
    @Column(name = "name", nullable = false)
    var name: String = name
        protected set
}
