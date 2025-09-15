package com.spectrum.workfolio.domain.entity.primary

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.model.AccountType
import jakarta.persistence.*

@Entity
@Table(
    name = "account",
    indexes = [
        Index(name = "idx_account_type", columnList = "type"),
        Index(name = "idx_account_provider_id", columnList = "provider_id"),
    ],
)
class Account(
    type: AccountType,
    email: String? = null,
    providerId: String,
    worker: Worker,
) : BaseEntity("AC") {
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: AccountType = type
        protected set

    @Column(name = "provider_id", nullable = false, unique = true)
    var providerId: String = providerId
        protected set

    @Column(name = "email", nullable = true)
    var email: String? = email
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set
}
