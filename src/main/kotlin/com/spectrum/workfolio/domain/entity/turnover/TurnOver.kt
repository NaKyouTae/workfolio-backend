package com.spectrum.workfolio.domain.entity.turnover

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
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

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    @JoinColumn(name = "turn_over_goal_id", nullable = false)
    var turnOverGoal: TurnOverGoal = turnOverGoal
        protected set

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    @JoinColumn(name = "turn_over_challenge_id", nullable = false)
    var turnOverChallenge: TurnOverChallenge = turnOverChallenge
        protected set

    @OneToOne(fetch = FetchType.LAZY, cascade = [CascadeType.REMOVE])
    @JoinColumn(name = "turn_over_retrospective_id", nullable = false)
    var turnOverRetrospective: TurnOverRetrospective = turnOverRetrospective
        protected set

    @Column(name = "started_at", nullable = true)
    var startedAt: LocalDate? = startedAt
        protected set

    @Column(name = "ended_at", nullable = true)
    var endedAt: LocalDate? = endedAt
        protected set

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
