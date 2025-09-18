package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.primary.Certifications
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CertificationsRepository : JpaRepository<Certifications, String> {
    fun findByNameAndWorkerId(name: String, workerId: String): Certifications?
    fun findByWorkerId(workerId: String): List<Certifications>
}
