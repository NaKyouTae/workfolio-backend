package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.interview.JobSearchCompany
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JobSearchCompanyRepository : JpaRepository<JobSearchCompany, String> {
    fun findByJobSearchIdOrderByAppliedAtDescClosedAtDesc(jobSearchId: String): List<JobSearchCompany>
}
