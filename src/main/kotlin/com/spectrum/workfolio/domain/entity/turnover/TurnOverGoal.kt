package com.spectrum.workfolio.domain.entity.turnover

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(
    name = "turn_over_goals",
    indexes = [],
)
class TurnOverGoal(
    reason: String,
    goal: String,
) : BaseEntity("TG") {
    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    var reason: String = reason
        protected set

    @Column(name = "goal", columnDefinition = "TEXT", nullable = false)
    var goal: String = goal
        protected set

    @OneToMany(mappedBy = "turnOverGoal", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    private var mutableSelfIntroductions: MutableList<SelfIntroduction> = mutableListOf()
    val selfIntroductions: List<SelfIntroduction> get() = mutableSelfIntroductions.toList()

    @OneToMany(mappedBy = "turnOverGoal", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    private var mutableInterviewQuestions: MutableList<InterviewQuestion> = mutableListOf()
    val interviewQuestions: List<InterviewQuestion> get() = mutableInterviewQuestions.toList()

    @OneToMany(mappedBy = "turnOverGoal", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    private var mutableCheckList: MutableList<CheckList> = mutableListOf()
    val checkList: List<CheckList> get() = mutableCheckList.toList()

    fun addSelfIntroduction(selfIntroduction: SelfIntroduction) {
        mutableSelfIntroductions.add(selfIntroduction)
    }

    fun addInterviewQuestion(interviewQuestion: InterviewQuestion) {
        mutableInterviewQuestions.add(interviewQuestion)
    }

    fun addCheckList(checkList: CheckList) {
        mutableCheckList.add(checkList)
    }

    // Cascade를 위한 컬렉션 동기화 메서드
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

    fun changeInfo(
        reason: String,
        goal: String,
    ) {
        this.reason = reason
        this.goal = goal
    }
}
