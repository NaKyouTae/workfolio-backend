package com.spectrum.workfolio.domain.entity.turnover

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(
    name = "interview_questions",
    indexes = [
        Index(name = "idx_interview_questions_turn_over_goal_id", columnList = "turn_over_goal_id"),
    ],
)
class InterviewQuestion(
    question: String,
    answer: String,
    turnOverGoal: TurnOverGoal,
) : BaseEntity("SI") {
    @Column(name = "question", columnDefinition = "TEXT", nullable = false)
    var question: String = question
        protected set

    @Column(name = "answer", columnDefinition = "TEXT", nullable = false)
    var answer: String = answer
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turn_over_goal_id", nullable = false)
    var turnOverGoal: TurnOverGoal = turnOverGoal
        protected set
}
