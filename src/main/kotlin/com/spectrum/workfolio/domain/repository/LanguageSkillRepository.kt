package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.LanguageSkill
import com.spectrum.workfolio.domain.enums.Language
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface LanguageSkillRepository : JpaRepository<LanguageSkill, String> {
    
    fun findByResumeId(resumeId: String): List<LanguageSkill>
    
    @Query("SELECT ls FROM LanguageSkill ls WHERE ls.resume.worker.id = :workerId")
    fun findByWorkerId(@Param("workerId") workerId: String): List<LanguageSkill>
    
    @Query("SELECT ls FROM LanguageSkill ls WHERE ls.resume.id = :resumeId AND ls.isVisible = true")
    fun findVisibleByResumeId(@Param("resumeId") resumeId: String): List<LanguageSkill>
    
    fun findByResumeIdAndLanguage(resumeId: String, language: Language): LanguageSkill?
}
