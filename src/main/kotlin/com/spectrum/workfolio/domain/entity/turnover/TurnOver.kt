package com.spectrum.workfolio.domain.entity.turnover

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(
    name = "turn_overs",
    indexes = [
        Index(name = "idx_turn_overs_worker_id", columnList = "worker_id"),
    ],
)
class TurnOver(
    name: String,
    turnOverGoal: TurnOverGoal,
    turnOverChallenge: TurnOverChallenge,
    turnOverRetrospective: TurnOverRetrospective,
    worker: Worker,
    startedAt: LocalDate? = null,
    endedAt: LocalDate? = null,
) : BaseEntity("TO") {
    @Column(name = "name", length = 1024, nullable = false)
    var name: String = name
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set

    @Embedded
    var turnOverGoal: TurnOverGoal = turnOverGoal
        protected set

    @Embedded
    var turnOverChallenge: TurnOverChallenge = turnOverChallenge
        protected set

    @Embedded
    var turnOverRetrospective: TurnOverRetrospective = turnOverRetrospective
        protected set

    @OneToMany(mappedBy = "turnOver", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    private var mutableSelfIntroductions: MutableList<SelfIntroduction> = mutableListOf()
    val selfIntroductions: List<SelfIntroduction> get() = mutableSelfIntroductions.toList()

    @OneToMany(mappedBy = "turnOver", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    private var mutableInterviewQuestions: MutableList<InterviewQuestion> = mutableListOf()
    val interviewQuestions: List<InterviewQuestion> get() = mutableInterviewQuestions.toList()

    @OneToMany(mappedBy = "turnOver", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    private var mutableCheckList: MutableList<CheckList> = mutableListOf()
    val checkList: List<CheckList> get() = mutableCheckList.toList()

    @OneToMany(mappedBy = "turnOver", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    private var mutableJobApplications: MutableList<JobApplication> = mutableListOf()
    val jobApplications: List<JobApplication> get() = mutableJobApplications.toList()

    @Column(name = "started_at", nullable = true)
    var startedAt: LocalDate? = startedAt
        protected set

    @Column(name = "ended_at", nullable = true)
    var endedAt: LocalDate? = endedAt
        protected set

    fun addCheckList(checkList: CheckList) {
        mutableCheckList.add(checkList)
    }

    fun syncSelfIntroductions(newSelfIntroductions: List<SelfIntroduction>) {
        mutableSelfIntroductions.clear()
        mutableSelfIntroductions.addAll(newSelfIntroductions)
    }

    fun syncInterviewQuestions(newInterviewQuestions: List<InterviewQuestion>) {
        mutableInterviewQuestions.clear()
        mutableInterviewQuestions.addAll(newInterviewQuestions)
    }

    fun syncCheckLists(newCheckLists: List<CheckList>) {
        mutableCheckList.clear()
        mutableCheckList.addAll(newCheckLists)
    }

    fun syncJobApplications(newJobApplications: List<JobApplication>) {
        mutableJobApplications.clear()
        mutableJobApplications.addAll(newJobApplications)
    }

    fun changeInfo(
        name: String,
        startedAt: LocalDate? = null,
        endedAt: LocalDate? = null,
    ) {
        this.name = name
        this.startedAt = startedAt
        this.endedAt = endedAt
    }
}
