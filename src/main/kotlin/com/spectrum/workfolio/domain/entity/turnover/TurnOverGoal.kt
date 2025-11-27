package com.spectrum.workfolio.domain.entity.turnover

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class TurnOverGoal(
    reason: String,
    goal: String,
) {
    @Column(name = "goal_reason", columnDefinition = "TEXT", nullable = false)
    var reason: String = reason
        protected set

    @Column(name = "goal_goal", columnDefinition = "TEXT", nullable = false)
    var goal: String = goal
        protected set

    fun changeInfo(
        reason: String,
        goal: String,
    ) {
        this.reason = reason
        this.goal = goal
    }
}
