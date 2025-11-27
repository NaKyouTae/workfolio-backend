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
        Index(name = "idx_self_introductions_turn_over_id", columnList = "turn_over_id"),
    ],
)
class SelfIntroduction(
    question: String,
    content: String,
    turnOver: TurnOver,
    isVisible: Boolean = true,
    priority: Int = 0,
) : BaseEntity("SI") {
    @Column(name = "question", columnDefinition = "TEXT", nullable = false)
    var question: String = question
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
    @JoinColumn(name = "turn_over_id", nullable = false)
    var turnOver: TurnOver = turnOver
        protected set

    fun changeInfo(question: String, content: String, isVisible: Boolean, priority: Int) {
        this.question = question
        this.content = content
        this.isVisible = isVisible
        this.priority = priority
    }
}
