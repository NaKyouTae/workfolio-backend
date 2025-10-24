package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.resume.LanguageTest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface LanguageTestRepository : JpaRepository<LanguageTest, String> {

    fun findByLanguageSkillIdOrderByPriorityAsc(languageSkillId: String): List<LanguageTest>

    @Query(
        "SELECT lt FROM LanguageTest lt WHERE lt.languageSkill.id = :languageSkillId AND lt.isVisible = true ORDER BY lt.priority ASC",
    )
    fun findVisibleByLanguageSkillId(
        @Param("languageSkillId") languageSkillId: String,
    ): List<LanguageTest>
}
