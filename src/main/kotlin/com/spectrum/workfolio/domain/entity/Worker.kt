package com.spectrum.workfolio.domain.entity

import com.spectrum.workfolio.domain.entity.history.Company
import com.spectrum.workfolio.domain.entity.history.Position
import com.spectrum.workfolio.domain.entity.history.Salary
import com.spectrum.workfolio.domain.entity.primary.Account
import com.spectrum.workfolio.domain.entity.primary.Certifications
import com.spectrum.workfolio.domain.entity.primary.Degrees
import com.spectrum.workfolio.domain.entity.primary.Education
import com.spectrum.workfolio.domain.entity.record.Record
import com.spectrum.workfolio.domain.entity.record.RecordGroup
import com.spectrum.workfolio.domain.entity.record.WorkerRecordGroup
import jakarta.persistence.*

/**
 * 직장인
 */
@Entity
@Table(
    name = "worker",
    indexes = [
        Index(name = "idx_worker_name", columnList = "name"),
        Index(name = "idx_worker_nick_name", columnList = "nick_name"),
    ]
)
class Worker(
    name: String,
    nickName: String? = null,
): BaseEntity("WK") {
    @Column(name = "name", length = 256, nullable = false)
    var name: String = name
        protected set

    @Column(name = "nick_name", length = 512, nullable = false, unique = true)
    var nickName: String? = nickName
        protected set

    @OneToMany(mappedBy = "worker", cascade = [CascadeType.REMOVE])
    private var mutableCompanies: MutableList<Company> = mutableListOf()
    val companies: List<Company> get() = mutableCompanies.toList()

    @OneToMany(mappedBy = "worker", cascade = [CascadeType.REMOVE])
    private var mutableCertifications: MutableList<Certifications> = mutableListOf()
    val certifications: List<Certifications> get() = mutableCertifications.toList()

    @OneToMany(mappedBy = "worker", cascade = [CascadeType.REMOVE])
    private var mutableDegrees: MutableList<Degrees> = mutableListOf()
    val degrees: List<Degrees> get() = mutableDegrees.toList()

    @OneToMany(mappedBy = "worker", cascade = [CascadeType.REMOVE])
    private var mutableEducations: MutableList<Education> = mutableListOf()
    val educations: List<Education> get() = mutableEducations.toList()

    @OneToMany(mappedBy = "worker", cascade = [CascadeType.REMOVE])
    private var mutableRecords: MutableList<Record> = mutableListOf()
    val records: List<Record> get() = mutableRecords.toList()

    @OneToMany(mappedBy = "worker", cascade = [CascadeType.REMOVE])
    private var mutableWorkerRecordGroups: MutableList<WorkerRecordGroup> = mutableListOf()
    val workerRecordGroups: List<WorkerRecordGroup> get() = mutableWorkerRecordGroups.toList()

    @OneToMany(mappedBy = "worker", cascade = [CascadeType.REMOVE])
    private var mutableRecordGroup: MutableList<RecordGroup> = mutableListOf()
    val recordGroups: List<RecordGroup> get() = mutableRecordGroup.toList()

    @OneToMany(mappedBy = "worker", cascade = [CascadeType.REMOVE])
    private var mutableAccounts: MutableList<Account> = mutableListOf()
    val accounts: List<Account> get() = mutableAccounts.toList()

    fun changeNickName(nickName: String) {
        this.nickName = nickName
    }
}
