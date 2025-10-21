package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.LanguageTest
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.LanguageTestRepository
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LanguageTestService(
    private val languageSkillService: LanguageSkillService,
    private val languageTestRepository: LanguageTestRepository,
) {

    @Transactional(readOnly = true)
    fun getLanguageTest(id: String): LanguageTest {
        return languageTestRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_LANGUAGE_TEST.message) }
    }

    @Transactional
    fun createLanguageTest(
        languageSkillId: String,
        testName: String?,
        score: String?,
        acquiredAt: Long?,
        isVisible: Boolean?,
    ): LanguageTest {
        val languageSkill = languageSkillService.getLanguageSkill(languageSkillId)
        val languageTest = LanguageTest(
            name = testName,
            score = score,
            acquiredAt = if (acquiredAt != null && acquiredAt > 0) TimeUtil.ofEpochMilli(acquiredAt).toLocalDate() else null,
            isVisible = isVisible,
            languageSkill = languageSkill,
        )

        return languageTestRepository.save(languageTest)
    }

    @Transactional
    fun updateLanguageTest(
        id: String,
        testName: String?,
        score: String?,
        acquiredAt: Long?,
        isVisible: Boolean?,
    ): LanguageTest {
        val languageTest = this.getLanguageTest(id)

        languageTest.changeInfo(
            name = testName,
            score = score,
            acquiredAt = if (acquiredAt != null && acquiredAt > 0) TimeUtil.ofEpochMilli(acquiredAt).toLocalDate() else null,
            isVisible = isVisible,
        )

        return languageTestRepository.save(languageTest)
    }

    @Transactional
    fun deleteLanguageTest(id: String) {
        val languageTest = this.getLanguageTest(id)
        languageTestRepository.delete(languageTest)
    }

    @Transactional
    fun deleteLanguageTestsByLanguageSkillId(languageSkillId: String) {
        val languageTests = languageTestRepository.findByLanguageSkillId(languageSkillId)
        languageTestRepository.deleteAll(languageTests)
    }
}
