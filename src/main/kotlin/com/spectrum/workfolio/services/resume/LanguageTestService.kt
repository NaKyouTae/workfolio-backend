package com.spectrum.workfolio.services.resume

import com.spectrum.workfolio.domain.entity.resume.LanguageSkill
import com.spectrum.workfolio.domain.entity.resume.LanguageTest
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.LanguageTestRepository
import com.spectrum.workfolio.proto.resume.ResumeUpdateRequest
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
        return languageTestRepository.findById(id).orElseThrow {
            WorkfolioException(MsgKOR.NOT_FOUND_LANGUAGE_TEST.message)
        }
    }

    @Transactional
    fun createLanguageTest(
        languageSkillId: String,
        name: String? = null,
        score: String? = null,
        acquiredAt: Long? = null,
        isVisible: Boolean,
        priority: Int = 0,
    ): LanguageTest {
        val languageSkill = languageSkillService.getLanguageSkill(languageSkillId)
        val languageTest = LanguageTest(
            name = name ?: "",
            score = score ?: "",
            acquiredAt = TimeUtil.ofEpochMilliNullable(acquiredAt)?.toLocalDate(),
            isVisible = isVisible,
            priority = priority,
            languageSkill = languageSkill,
        )

        return languageTestRepository.save(languageTest)
    }

    @Transactional
    fun createBulkLanguageTest(
        languageSkill: LanguageSkill,
        languageTests: List<LanguageTest>,
    ) {
        val newLanguageTests = languageTests.map {
            LanguageTest(
                name = it.name,
                score = it.score,
                acquiredAt = it.acquiredAt,
                isVisible = it.isVisible,
                priority = it.priority,
                languageSkill = languageSkill,
            )
        }

        languageTestRepository.saveAll(newLanguageTests)
    }

    @Transactional
    fun createBulkLanguageTest(
        languageSkillId: String,
        requests: List<ResumeUpdateRequest.LanguageSkillRequest.LanguageTestRequest>,
    ): List<LanguageTest> {
        val languageSkill = languageSkillService.getLanguageSkill(languageSkillId)
        val entities = requests.map { request ->
            LanguageTest(
                name = request.name,
                score = request.score,
                acquiredAt = TimeUtil.ofEpochMilliNullable(request.acquiredAt)?.toLocalDate(),
                isVisible = request.isVisible,
                priority = request.priority,
                languageSkill = languageSkill,
            )
        }

        return languageTestRepository.saveAll(entities)
    }

    @Transactional
    fun updateLanguageTest(
        id: String,
        name: String? = null,
        score: String? = null,
        acquiredAt: Long? = null,
        isVisible: Boolean,
        priority: Int = 0,
    ): LanguageTest {
        val languageTest = this.getLanguageTest(id)

        languageTest.changeInfo(
            name = name ?: "",
            score = score ?: "",
            acquiredAt = TimeUtil.ofEpochMilliNullable(acquiredAt)?.toLocalDate(),
            isVisible = isVisible,
            priority = priority,
        )

        return languageTestRepository.save(languageTest)
    }

    @Transactional
    fun updateBulkLanguageTest(
        languageSkillId: String,
        requests: List<ResumeUpdateRequest.LanguageSkillRequest.LanguageTestRequest>,
    ): List<LanguageTest> {
        val languageSkill = languageSkillService.getLanguageSkill(languageSkillId)
        val existingLanguageTests = languageTestRepository.findByLanguageSkillIdOrderByPriorityAsc(languageSkillId)

        val requestMap = requests.filter { it.id.isNullOrEmpty() }.associateBy { it.id }

        val updatedEntities = existingLanguageTests.mapNotNull { entity ->
            requestMap[entity.id]?.let { request ->
                entity.changeInfo(
                    name = request.name,
                    score = request.score,
                    acquiredAt = TimeUtil.ofEpochMilliNullable(request.acquiredAt)?.toLocalDate(),
                    isVisible = request.isVisible,
                    priority = request.priority,
                )
                entity
            }
        }

        return languageTestRepository.saveAll(updatedEntities)
    }

    @Transactional
    fun deleteLanguageTest(id: String) {
        val languageTest = this.getLanguageTest(id)
        languageTestRepository.delete(languageTest)
    }

    @Transactional
    fun deleteLanguageTests(languageTestIds: List<String>) {
        if (languageTestIds.isNotEmpty()) {
            languageTestRepository.deleteAllById(languageTestIds)
        }
    }

    @Transactional
    fun deleteLanguageTestsByLanguageSkillId(languageSkillId: String) {
        val languageTests = languageTestRepository.findByLanguageSkillIdOrderByPriorityAsc(languageSkillId)
        languageTestRepository.deleteAll(languageTests)
    }
}
