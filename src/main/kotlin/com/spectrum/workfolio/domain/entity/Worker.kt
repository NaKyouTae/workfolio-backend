package com.spectrum.workfolio.domain.entity

import com.spectrum.workfolio.domain.entity.primary.Account
import com.spectrum.workfolio.domain.entity.record.Record
import com.spectrum.workfolio.domain.entity.record.RecordGroup
import com.spectrum.workfolio.domain.entity.record.WorkerRecordGroup
import com.spectrum.workfolio.domain.entity.resume.Resume
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

/**
 * 직장인
 */
@Entity
@Table(
    name = "workers",
    indexes = [
        Index(name = "idx_workers_name", columnList = "name"),
        Index(name = "idx_workers_nick_name", columnList = "nick_name"),
    ],
)
class Worker(
    name: String,
    nickName: String? = null,
) : BaseEntity("WK") {
    @Column(name = "name", length = 256, nullable = false)
    var name: String = name
        protected set

    @Column(name = "nick_name", length = 512, nullable = false, unique = true)
    var nickName: String? = nickName
        protected set

    @OneToMany(mappedBy = "worker", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableResumes: MutableList<Resume> = mutableListOf()
    val resumes: List<Resume> get() = mutableResumes.toList()

    @OneToMany(mappedBy = "worker", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableRecords: MutableList<Record> = mutableListOf()
    val records: List<Record> get() = mutableRecords.toList()

    @OneToMany(mappedBy = "worker", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableWorkerRecordGroups: MutableList<WorkerRecordGroup> = mutableListOf()
    val workerRecordGroups: List<WorkerRecordGroup> get() = mutableWorkerRecordGroups.toList()

    @OneToMany(mappedBy = "worker", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableRecordGroup: MutableList<RecordGroup> = mutableListOf()
    val recordGroups: List<RecordGroup> get() = mutableRecordGroup.toList()

    @OneToMany(mappedBy = "worker", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableAccounts: MutableList<Account> = mutableListOf()
    val accounts: List<Account> get() = mutableAccounts.toList()

    fun changeNickName(nickName: String) {
        this.nickName = nickName
    }
}
