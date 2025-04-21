package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.history.Company
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CompanyRepository: JpaRepository<Company, String> {
}
