package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.Resume
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ResumeRepository : JpaRepository<Resume, String> {
    
    fun findByWorkerId(workerId: String): List<Resume>
    
    fun findByPublicId(publicId: String): Resume?
    
    fun findByIsPublicTrue(): List<Resume>
    
    @Query("SELECT r FROM Resume r WHERE r.worker.id = :workerId AND r.isDefault = true")
    fun findDefaultByWorkerId(@Param("workerId") workerId: String): Resume?
    
    @Query("SELECT r FROM Resume r WHERE r.worker.id = :workerId AND r.isPublic = true")
    fun findPublicByWorkerId(@Param("workerId") workerId: String): List<Resume>
}