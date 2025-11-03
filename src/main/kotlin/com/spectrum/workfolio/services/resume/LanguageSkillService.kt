package com.spectrum.workfolio.services.resume

import com.spectrum.workfolio.domain.entity.resume.LanguageSkill
import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.Language
import com.spectrum.workfolio.domain.enums.LanguageLevel
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.LanguageSkillRepository
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LanguageSkillService(
    private val resumeQueryService: ResumeQueryService,
    private val languageSkillRepository: LanguageSkillRepository,
) {

    @Transactional(readOnly = true)
    fun getLanguageSkill(id: String): LanguageSkill {
        return languageSkillRepository.findById(id).orElseThrow {
            WorkfolioException(MsgKOR.NOT_FOUND_LANGUAGE_SKILL.message)
        }
    }

    @Transactional(readOnly = true)
    fun listLanguageSkills(resumeId: String): List<LanguageSkill> {
        return languageSkillRepository.findByResumeIdOrderByPriorityAsc(resumeId)
    }

    @Transactional
    fun createLanguageSkill(
        resumeId: String,
        language: Language? = null,
        level: LanguageLevel? = null,
        isVisible: Boolean,
        priority: Int,
    ): LanguageSkill {
        val resume = resumeQueryService.getResume(resumeId)
        val languageSkill = LanguageSkill(
            language = language,
            level = level,
            isVisible = isVisible,
            priority = priority,
            resume = resume,
        )

        return languageSkillRepository.save(languageSkill)
    }

    @Transactional
    fun createLanguageSkill(
        resume: Resume,
        languageSkill: LanguageSkill,
    ): LanguageSkill {
        val newLanguageSkill = LanguageSkill(
            language = languageSkill.language,
            level = languageSkill.level,
            isVisible = languageSkill.isVisible,
            priority = languageSkill.priority,
            resume = resume,
        )

        return languageSkillRepository.save(newLanguageSkill)
    }

    @Transactional
    fun updateLanguageSkill(
        id: String,
        language: Language? = null,
        level: LanguageLevel? = null,
        isVisible: Boolean,
        priority: Int = 0,
    ): LanguageSkill {
        val languageSkill = this.getLanguageSkill(id)

        languageSkill.changeInfo(
            language = language,
            level = level,
            isVisible = isVisible,
            priority = priority,
        )

        return languageSkillRepository.save(languageSkill)
    }

    @Transactional
    fun deleteLanguageSkill(id: String) {
        val languageSkill = this.getLanguageSkill(id)
        languageSkillRepository.delete(languageSkill)
    }

    @Transactional
    fun deleteLanguageSkills(languageSkillIds: List<String>) {
        if (languageSkillIds.isNotEmpty()) {
            languageSkillRepository.deleteAllById(languageSkillIds)
        }
    }

    @Transactional
    fun deleteLanguageSkillsByResumeId(resumeId: String) {
        val languageSkills = languageSkillRepository.findByResumeIdOrderByPriorityAsc(resumeId)
        languageSkillRepository.deleteAll(languageSkills)
    }
}
