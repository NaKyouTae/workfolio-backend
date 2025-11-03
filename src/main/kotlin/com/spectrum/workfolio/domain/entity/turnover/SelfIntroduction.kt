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
    name = "self_introductions",
    indexes = [
        Index(name = "idx_self_introductions_turn_over_goal_id", columnList = "turn_over_goal_id"),
    ],
)
class SelfIntroduction(
    question: String,
    content: String,
    turnOverGoal: TurnOverGoal,
) : BaseEntity("SI") {
    @Column(name = "question", columnDefinition = "TEXT", nullable = false)
    var question: String = question
        protected set

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    var content: String = content
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turn_over_goal_id", nullable = false)
    var turnOverGoal: TurnOverGoal = turnOverGoal
        protected set
}
