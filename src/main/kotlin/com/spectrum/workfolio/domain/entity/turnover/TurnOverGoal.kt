package com.spectrum.workfolio.domain.entity.turnover

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(
    name = "turn_over_goals",
    indexes = [
        Index(name = "idx_turn_over_goals_turn_over_id", columnList = "turn_over_id"),
    ],
)
class TurnOverGoal(
    reason: String,
    goal: String,
    turnOver: TurnOver,
) : BaseEntity("TG") {
    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    var reason: String = reason
        protected set

    @Column(name = "goal", columnDefinition = "TEXT", nullable = false)
    var goal: String = goal
        protected set

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turn_over_id", nullable = false)
    var turnOver: TurnOver = turnOver
        protected set

    @OneToMany(mappedBy = "turnOverGoal", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableSelfIntroductions: MutableList<SelfIntroduction> = mutableListOf()
    val selfIntroductions: List<SelfIntroduction> get() = mutableSelfIntroductions.toList()

    @OneToMany(mappedBy = "turnOverGoal", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableInterviewQuestions: MutableList<InterviewQuestion> = mutableListOf()
    val interviewQuestions: List<InterviewQuestion> get() = mutableInterviewQuestions.toList()

    @OneToMany(mappedBy = "turnOverGoal", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableCheckList: MutableList<CheckList> = mutableListOf()
    val checkList: List<CheckList> get() = mutableCheckList.toList()
}
