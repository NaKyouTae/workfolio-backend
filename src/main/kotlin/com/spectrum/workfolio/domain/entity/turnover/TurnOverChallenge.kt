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

    @OneToMany(
        mappedBy = "turnOverChallenge",
        cascade = [CascadeType.ALL],
        fetch = FetchType.LAZY,
        orphanRemoval = true,
    )
    private var mutableJobApplications: MutableList<JobApplication> = mutableListOf()
    val jobApplications: List<JobApplication> get() = mutableJobApplications.toList()

    // Cascade를 위한 컬렉션 동기화 메서드
    fun syncJobApplications(newJobApplications: List<JobApplication>) {
        mutableJobApplications.clear()
        mutableJobApplications.addAll(newJobApplications)
    }
}
