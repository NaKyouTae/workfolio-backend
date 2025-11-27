package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.Staff
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StaffRepository : JpaRepository<Staff, String> {
    fun findByUsername(username: String): Staff?
    fun findByEmail(email: String): Staff?
    fun existsByUsername(username: String): Boolean
    fun existsByEmail(email: String): Boolean
}
