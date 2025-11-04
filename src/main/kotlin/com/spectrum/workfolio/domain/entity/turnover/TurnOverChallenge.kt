package com.spectrum.workfolio.domain.entity.turnover

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(
    name = "turn_over_challenges",
    indexes = [],
)
class TurnOverChallenge : BaseEntity("TG") {

    @OneToMany(mappedBy = "turnOverChallenge", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableJobApplications: MutableList<JobApplication> = mutableListOf()
    val jobApplications: List<JobApplication> get() = mutableJobApplications.toList()
}
