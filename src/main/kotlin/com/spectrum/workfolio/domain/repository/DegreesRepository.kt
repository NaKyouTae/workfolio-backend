package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.primary.Degrees
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DegreesRepository: JpaRepository<Degrees, String> {
    fun findByNameAndWorkerId(name: String, workerId: String): Degrees?
    fun findByWorkerId(workerId: String): List<Degrees>
}
