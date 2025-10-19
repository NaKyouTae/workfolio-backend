package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.Certifications
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CertificationsRepository : JpaRepository<Certifications, String> {
    fun findByResumeIdOrderByIssuedAtDesc(resumeId: String): List<Certifications>
}
