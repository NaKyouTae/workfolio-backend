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
) : BaseEntity("TO") {
    @Column(name = "name", length = 1024, nullable = false)
    var name: String = name
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set

    @OneToOne(mappedBy = "turnOver", cascade = [CascadeType.REMOVE])
    var turnOverGoal: TurnOverGoal = turnOverGoal
        protected set

    @OneToOne(mappedBy = "turnOver", cascade = [CascadeType.REMOVE])
    var turnOverChallenge: TurnOverChallenge = turnOverChallenge
        protected set

    @OneToOne(mappedBy = "turnOver", cascade = [CascadeType.REMOVE])
    var turnOverRetrospective: TurnOverRetrospective = turnOverRetrospective
        protected set
}
