package com.spectrum.workfolio.domain.entity

import com.spectrum.workfolio.domain.enums.SystemConfigType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(
    name = "system_configs",
    indexes = [
        Index(name = "idx_system_configs_type_worker_id", columnList = "type, worker_id"),
    ],
)
class SystemConfig(
    type: SystemConfigType,
    value: String,
    worker: Worker,
) : BaseEntity("SC") {

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 512, nullable = false)
    var type: SystemConfigType = type
        protected set

    @Column(name = "value", length = 1024, nullable = false)
    var value: String = value
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set

    fun changeInfo(type: SystemConfigType, value: String) {
        this.type = type
        this.value = value.uppercase()
    }
}
