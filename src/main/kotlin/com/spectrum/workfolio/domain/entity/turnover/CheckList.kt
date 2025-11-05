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
    name = "check_list",
    indexes = [
        Index(name = "idx_check_list_turn_over_goal_id", columnList = "turn_over_goal_id"),
    ],
)
class CheckList(
    checked: Boolean,
    content: String,
    turnOverGoal: TurnOverGoal,
    isVisible: Boolean = true,
    priority: Int = 0,
) : BaseEntity("CL") {
    @Column(name = "checked", nullable = false)
    var checked: Boolean = checked
        protected set

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    var content: String = content
        protected set

    @Column(name = "is_visible", nullable = false)
    var isVisible: Boolean = isVisible
        protected set

    @Column(name = "priority", nullable = false)
    var priority: Int = priority
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turn_over_goal_id", nullable = false)
    var turnOverGoal: TurnOverGoal = turnOverGoal
        protected set

    fun changeInfo(
        checked: Boolean,
        content: String,
        isVisible: Boolean,
        priority: Int,
    ) {
        this.checked = checked
        this.content = content
        this.isVisible = isVisible
        this.priority = priority
    }
}
