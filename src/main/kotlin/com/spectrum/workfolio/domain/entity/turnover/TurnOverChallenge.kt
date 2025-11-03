package com.spectrum.workfolio.domain.entity.turnover

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(
    name = "turn_over_challenges",
    indexes = [
        Index(name = "idx_turn_over_challenges_turn_over_id", columnList = "turn_over_id"),
    ],
)
class TurnOverChallenge(
    turnOver: TurnOver,
) : BaseEntity("TG") {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turn_over_id", nullable = false)
    var turnOver: TurnOver = turnOver
        protected set

    @OneToMany(mappedBy = "turnOverChallenge", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableJobApplications: MutableList<JobApplication> = mutableListOf()
    val jobApplications: List<JobApplication> get() = mutableJobApplications.toList()
}
