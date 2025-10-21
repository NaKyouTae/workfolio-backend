package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.LanguageSkill
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
        return languageSkillRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_LANGUAGE_SKILL.message) }
    }

    @Transactional(readOnly = true)
    fun listLanguageSkills(resumeId: String): List<LanguageSkill> {
        return languageSkillRepository.findByResumeId(resumeId)
    }

    @Transactional
    fun createLanguageSkill(
        resumeId: String,
        language: Language?,
        level: LanguageLevel?,
        isVisible: Boolean?,
    ): LanguageSkill {
        val resume = resumeQueryService.getResume(resumeId)
        val languageSkill = LanguageSkill(
            language = language,
            level = level,
            isVisible = isVisible,
            resume = resume,
        )

        return languageSkillRepository.save(languageSkill)
    }

    @Transactional
    fun updateLanguageSkill(
        id: String,
        language: Language?,
        level: LanguageLevel?,
        isVisible: Boolean?,
    ): LanguageSkill {
        val languageSkill = this.getLanguageSkill(id)

        languageSkill.changeInfo(
            language = language,
            level = level,
            isVisible = isVisible,
        )

        return languageSkillRepository.save(languageSkill)
    }

    @Transactional
    fun deleteLanguageSkill(id: String) {
        val languageSkill = this.getLanguageSkill(id)
        languageSkillRepository.delete(languageSkill)
    }

    @Transactional
    fun deleteLanguageSkillsByResumeId(resumeId: String) {
        val languageSkills = languageSkillRepository.findByResumeId(resumeId)
        languageSkillRepository.deleteAll(languageSkills)
    }
}
