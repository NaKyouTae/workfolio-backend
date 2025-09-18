package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.history.Company
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface CompanyRepository: JpaRepository<Company, String> {
    fun findByNameAndWorkerId(name: String, workerId: String): Company?
    @Query("SELECT c FROM Company c WHERE c.worker.id = :workerId")
    fun findByWorkerId(workerId: String): List<Company>
}
