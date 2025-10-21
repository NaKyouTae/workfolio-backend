package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.Link
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LinkRepository : JpaRepository<Link, String> {
    fun findByResumeId(resumeId: String): List<Link>
}
