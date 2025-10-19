package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.Resume
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ResumeRepository : JpaRepository<Resume, String> {
    fun findByWorkerId(workerId: String): List<Resume>
}
