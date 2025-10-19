package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.Project
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectRepository : JpaRepository<Project, String> {
    fun findByCompanyId(companyId: String): List<Project>
}
