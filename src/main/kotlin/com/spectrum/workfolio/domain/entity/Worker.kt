package com.spectrum.workfolio.domain.entity

import com.spectrum.workfolio.domain.entity.primary.Account
import com.spectrum.workfolio.domain.entity.record.RecordGroup
import com.spectrum.workfolio.domain.entity.record.WorkerRecordGroup
import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.Gender
import com.spectrum.workfolio.domain.enums.WorkerStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.LocalDate

/**
 * 직장인
 */
@Entity
@Table(
    name = "workers",
    indexes = [
        Index(name = "idx_workers_nick_name", columnList = "nick_name"),
        Index(name = "idx_workers_phone", columnList = "phone"),
        Index(name = "idx_workers_email", columnList = "email"),
        Index(name = "idx_workers_status", columnList = "status"),
    ],
)
class Worker(
    status: WorkerStatus,
    nickName: String,
    phone: String,
    email: String,
    birthDate: LocalDate? = null,
    gender: Gender? = null,
) : BaseEntity("WK") {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32, nullable = false)
    var status: WorkerStatus = status
        protected set

    @Column(name = "nick_name", length = 512, nullable = false, unique = true)
    var nickName: String = nickName
        protected set

    @Column(name = "phone", length = 32, nullable = false, unique = true)
    var phone: String = phone
        protected set

    @Column(name = "email", length = 512, nullable = false, unique = true)
    var email: String = email
        protected set

    @Column(name = "birth_date", nullable = true)
    var birthDate: LocalDate? = birthDate
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 24, nullable = true)
    var gender: Gender? = gender
        protected set

    @Column(name = "credit", nullable = false)
    var credit: Int = 0
        protected set

    @Version
    @Column(name = "credit_version", nullable = false)
    var creditVersion: Long = 0
        protected set

    @OneToMany(mappedBy = "worker", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableResumes: MutableList<Resume> = mutableListOf()
    val resumes: List<Resume> get() = mutableResumes.toList()

    @OneToMany(mappedBy = "worker", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableWorkerRecordGroups: MutableList<WorkerRecordGroup> = mutableListOf()
    val workerRecordGroups: List<WorkerRecordGroup> get() = mutableWorkerRecordGroups.toList()

    @OneToMany(mappedBy = "worker", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableRecordGroup: MutableList<RecordGroup> = mutableListOf()
    val recordGroups: List<RecordGroup> get() = mutableRecordGroup.toList()

    @OneToMany(mappedBy = "worker", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableAccounts: MutableList<Account> = mutableListOf()
    val accounts: List<Account> get() = mutableAccounts.toList()

    fun changeInfo(
        nickName: String,
        phone: String,
        email: String,
        birthDate: LocalDate? = null,
        gender: Gender? = null,
    ) {
        this.nickName = nickName
        this.phone = phone
        this.email = email
        this.birthDate = birthDate
        this.gender = gender
    }

    fun changeNickName(nickName: String) {
        this.nickName = nickName
    }

    fun addCredits(amount: Int) {
        require(amount >= 0) { "Amount must be non-negative" }
        this.credit += amount
    }

    fun useCredits(amount: Int) {
        require(amount > 0) { "Amount must be positive" }
        require(this.credit >= amount) { "Insufficient credits" }
        this.credit -= amount
    }

    fun hasEnoughCredits(amount: Int): Boolean = this.credit >= amount
}
