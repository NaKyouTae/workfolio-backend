package com.spectrum.workfolio.domain.entity.primary

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.enums.AccountType
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
