package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.Achievement
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AchievementRepository : JpaRepository<Achievement, String> {
    
    fun findByCareerId(careerId: String): List<Achievement>
    
    @Query("SELECT a FROM Achievement a WHERE a.career.resume.worker.id = :workerId")
    fun findByWorkerId(@Param("workerId") workerId: String): List<Achievement>
    
    @Query("SELECT a FROM Achievement a WHERE a.career.id = :careerId AND a.isVisible = true")
    fun findVisibleByCareerId(@Param("careerId") careerId: String): List<Achievement>
}
