package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.Salary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SalaryRepository : JpaRepository<Salary, String> {
    fun findByCareerIdOrderByPriorityAscNegotiationDateDesc(careerId: String): List<Salary>
}
