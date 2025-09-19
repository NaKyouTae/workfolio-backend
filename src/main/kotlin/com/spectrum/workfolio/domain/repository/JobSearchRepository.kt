package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.interview.JobSearch
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JobSearchRepository : JpaRepository<JobSearch, String> {
    fun findByWorkerIdOrderByStartedAtDescEndedAtDesc(workerId: String): List<JobSearch>
}
