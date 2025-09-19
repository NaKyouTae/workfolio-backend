package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.history.Salary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SalaryRepository : JpaRepository<Salary, String> {
    fun findByCompanyId(companyId: String): List<Salary>
}
