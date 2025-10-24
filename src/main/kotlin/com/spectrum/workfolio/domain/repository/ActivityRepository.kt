package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.Activity
import com.spectrum.workfolio.domain.enums.ActivityType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ActivityRepository : JpaRepository<Activity, String> {

    fun findByResumeIdOrderByPriorityAsc(resumeId: String): List<Activity>

    @Query("SELECT a FROM Activity a WHERE a.resume.worker.id = :workerId ORDER BY a.priority ASC")
    fun findByWorkerId(@Param("workerId") workerId: String): List<Activity>

    @Query("SELECT a FROM Activity a WHERE a.resume.id = :resumeId AND a.isVisible = true ORDER BY a.priority ASC")
    fun findVisibleByResumeId(@Param("resumeId") resumeId: String): List<Activity>

    fun findByResumeIdAndTypeOrderByPriorityAsc(resumeId: String, type: ActivityType): List<Activity>
}
