package com.spectrum.workfolio.services.resume

import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.AttachmentTargetType
import com.spectrum.workfolio.domain.enums.Gender
import com.spectrum.workfolio.domain.repository.ResumeRepository
import com.spectrum.workfolio.proto.attachment.AttachmentRequest
import com.spectrum.workfolio.proto.resume.ResumeUpdateRequest
import com.spectrum.workfolio.services.AttachmentCommandService
import com.spectrum.workfolio.services.AttachmentQueryService
import com.spectrum.workfolio.services.WorkerService
import com.spectrum.workfolio.utils.EnumUtils.convertProtoEnumSafe
import com.spectrum.workfolio.utils.TimeUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Resume 명령 전용 서비스
 * Resume 생성, 수정, 삭제 등의 비즈니스 로직 처리
 */
@Service
class ResumeCommandService(
    private val workerService: WorkerService,
    private val careerService: CareerService,
    private val salaryService: SalaryService,
    private val projectService: ProjectService,
    private val activityService: ActivityService,
    private val resumeRepository: ResumeRepository,
    private val educationService: EducationService,
    private val resumeQueryService: ResumeQueryService,
    private val languageTestService: LanguageTestService,
    private val languageSkillService: LanguageSkillService,
    private val attachmentQueryService: AttachmentQueryService,
    private val attachmentCommandService: AttachmentCommandService,
) {

    fun createResume(workerId: String, request: ResumeUpdateRequest): Resume {
        val worker = workerService.getWorker(workerId)

        val resume = Resume(
            title = request.title,
            name = request.name,
            phone = request.phone,
            email = request.email,
            position = request.position,
            gender = convertProtoEnumSafe<Gender>(request.gender),
            birthDate = TimeUtil.ofEpochMilliNullable(request.birthDate)?.toLocalDate(),
            description = request.description,
            isPublic = request.isPublic,
            isDefault = request.isDefault,
            publicId = Resume.generatePublicId(),
            worker = worker,
        )

        return resumeRepository.save(resume)
    }

    @Transactional
    fun duplicateResume(resumeId: String): Resume {
        // 1. 원본 Resume 조회
        val originalResume = resumeQueryService.getResume(resumeId)

        // 2. 새로운 Resume 생성
        val duplicatedResume = Resume(
            title = "${originalResume.title} (복제본)",
            name = originalResume.name,
            position = originalResume.position,
            phone = originalResume.phone,
            email = originalResume.email,
            publicId = Resume.generatePublicId(), // 새로운 public ID
            isPublic = false, // 복사본은 비공개로 시작
            isDefault = false,
            description = originalResume.description,
            gender = originalResume.gender,
            birthDate = originalResume.birthDate,
            worker = originalResume.worker,
        )
        val savedResume = resumeRepository.save(duplicatedResume)

        // 3. 하위 엔티티 복제
        // 3-1. Education 복제
        educationService.createBulkEducation(savedResume, originalResume.educations)

        // 3-2. Career 복제 (Salary 포함)
        originalResume.careers.forEach {
            val savedCareers = careerService.createCareer(savedResume, it)

            salaryService.createBulkSalary(savedCareers, it.salaries)
        }

        // 3-3. Project 복제
        projectService.createBulkProjectFromEntity(savedResume, originalResume.projects)

        // 3-4. Activity 복제
        activityService.createBulkActivityFromEntity(savedResume, originalResume.activities)

        // 3-5. LanguageSkill 복제 (LanguageTest 포함)
        originalResume.languageSkills.forEach {
            val savedLanguageSkill = languageSkillService.createLanguageSkill(savedResume, it)
            // LanguageTest 복제
            languageTestService.createBulkLanguageTest(savedLanguageSkill, it.languageTests)
        }

        val originalAttachments = attachmentQueryService.listAttachments(originalResume.id)

        // 3-6. Attachment 복제
        val storagePath = "resumes/attachments"
        attachmentCommandService.createBulkAttachmentFromEntity(savedResume, storagePath, originalAttachments)

        return savedResume
    }

    @Transactional
    fun updateResume(workerId: String, request: ResumeUpdateRequest): Resume {
        val resume = upsertResume(workerId, request)

        // 학력 처리
        updateEducations(resume.id, request.educationsList)

        // 경력 처리
        updateCareers(resume.id, request.careersList)

        // 프로젝트 처리
        updateProjects(resume.id, request.projectsList)

        // 활동 처리
        updateActivities(resume.id, request.activitiesList)

        // 첨부파일 처리
        updateAttachments(resume.id, request.attachmentsList)

        // 언어 능력 처리
        updateLanguageSkills(resume.id, request.languagesList)

        return resumeRepository.save(resume)
    }

    @Transactional
    fun deleteResume(id: String) {
        val resume = resumeQueryService.getResume(id)
        resumeRepository.delete(resume)
    }

    private fun upsertResume(workerId: String, request: ResumeUpdateRequest): Resume {
        val resume = resumeQueryService.getResumeOptional(request.id)

        return if (resume != null) {
            resume.changeInfo(
                title = request.title,
                name = request.name,
                phone = request.phone,
                email = request.email,
                position = request.position,
                description = request.description,
                birthDate = TimeUtil.ofEpochMilliNullable(request.birthDate)?.toLocalDate(),
                gender = convertProtoEnumSafe<Gender>(request.gender),
                isPublic = request.isPublic,
                isDefault = request.isDefault,
            )

            resume
        } else {
            this.createResume(workerId, request)
        }
    }

    private fun updateEducations(
        resumeId: String,
        educationRequests: List<ResumeUpdateRequest.EducationRequest>,
    ) {
        val existingEducations = educationService.listEducations(resumeId).educationsList
        val requestIds = educationRequests.mapNotNull { it.id }.toSet()

        val toDelete = existingEducations.filter { it.id !in requestIds }
        val createRequests = educationRequests.filter { it.id.isNullOrEmpty() }
        val updateRequests = educationRequests.filter { !it.id.isNullOrEmpty() }

        educationService.deleteEducations(toDelete.map { it.id })
        educationService.createBulkEducation(resumeId, createRequests)
        educationService.updateBulkEducation(resumeId, updateRequests)
    }

    private fun updateCareers(
        resumeId: String,
        careerRequests: List<ResumeUpdateRequest.CareerRequest>,
    ) {
        val existingCareers = careerService.listCareers(resumeId).careersList
        val requestIds = careerRequests.filter { it.career.id.isNotEmpty() }.mapNotNull { it.career.id }.toSet()

        val toDelete = existingCareers.filter { it.id !in requestIds }
        val createRequests = careerRequests.filter { it.career.id.isNullOrEmpty() }.map { it.career }
        val updateRequests = careerRequests.filter { !it.career.id.isNullOrEmpty() }.map { it.career }

        careerService.deleteCareers(toDelete.map { it.id })
        val createdCareers = careerService.createBulkCareer(resumeId, createRequests)
        val updatedCareers = careerService.updateBulkCareer(resumeId, updateRequests)

        // 생성된 Career의 Salary 처리
        createdCareers.forEachIndexed { index, careerEntity ->
            val request = careerRequests.filter { it.career.id.isNullOrEmpty() }[index]
            request.salariesList.forEach { salaryRequest ->
                val salary = com.spectrum.workfolio.domain.entity.resume.Salary(
                    amount = salaryRequest.amount,
                    memo = salaryRequest.memo,
                    isVisible = salaryRequest.isVisible,
                    priority = salaryRequest.priority,
                    negotiationDate = TimeUtil.ofEpochMilliNullable(salaryRequest.negotiationDate)?.toLocalDate(),
                    career = careerEntity,
                )
                careerEntity.addSalary(salary)
            }
        }

        // 업데이트된 Career의 Salary 처리
        updatedCareers.forEach { careerEntity ->
            val request = careerRequests.find { it.career.id == careerEntity.id } ?: return@forEach
            val existingSalaries = careerEntity.salaries
            val requestSalaryIds = request.salariesList.mapNotNull { it.id }.filter { it.isNotBlank() }.toSet()

            val salariesToDelete = existingSalaries.filter { it.id !in requestSalaryIds }
            if (salariesToDelete.isNotEmpty()) {
                careerEntity.removeSalaries(salariesToDelete)
            }

            request.salariesList.forEach { salaryRequest ->
                val negotiationDate = TimeUtil.ofEpochMilliNullable(salaryRequest.negotiationDate)?.toLocalDate()
                if (salaryRequest.id.isNullOrEmpty()) {
                    val salary = com.spectrum.workfolio.domain.entity.resume.Salary(
                        amount = salaryRequest.amount,
                        memo = salaryRequest.memo,
                        isVisible = salaryRequest.isVisible,
                        priority = salaryRequest.priority,
                        negotiationDate = negotiationDate,
                        career = careerEntity,
                    )
                    careerEntity.addSalary(salary)
                } else {
                    val existingSalary = existingSalaries.find { it.id == salaryRequest.id }
                    existingSalary?.changeInfo(
                        amount = salaryRequest.amount,
                        negotiationDate = negotiationDate,
                        memo = salaryRequest.memo,
                        isVisible = salaryRequest.isVisible,
                        priority = salaryRequest.priority,
                    )
                }
            }
        }
    }

    private fun updateProjects(
        resumeId: String,
        projectRequests: List<ResumeUpdateRequest.ProjectRequest>,
    ) {
        val existingProjects = projectService.listProjects(resumeId)
        val requestIds = projectRequests.mapNotNull { it.id }.toSet()

        val toDelete = existingProjects.filter { it.id !in requestIds }
        val createRequests = projectRequests.filter { it.id.isNullOrEmpty() }
        val updateRequests = projectRequests.filter { !it.id.isNullOrEmpty() }

        projectService.deleteProjects(toDelete.map { it.id })
        projectService.createBulkProject(resumeId, createRequests)
        projectService.updateBulkProject(resumeId, updateRequests)
    }

    private fun updateActivities(
        resumeId: String,
        activityRequests: List<ResumeUpdateRequest.ActivityRequest>,
    ) {
        val existingActivities = activityService.listActivities(resumeId)
        val requestIds = activityRequests.mapNotNull { it.id }.toSet()

        val toDelete = existingActivities.filter { it.id !in requestIds }
        val createRequests = activityRequests.filter { it.id.isNullOrEmpty() }
        val updateRequests = activityRequests.filter { !it.id.isNullOrEmpty() }

        activityService.deleteActivities(toDelete.map { it.id })
        activityService.createBulkActivity(resumeId, createRequests)
        activityService.updateBulkActivity(resumeId, updateRequests)
    }

    private fun updateAttachments(
        targetId: String,
        attachmentRequests: List<AttachmentRequest>,
    ) {
        val storagePath = "resumes/attachments"
        val existingAttachments = attachmentQueryService.listAttachments(targetId)
        val existingIds = existingAttachments.map { it.id }.toSet()
        val requestIds = attachmentRequests.mapNotNull { it.id }.toSet()

        val toDelete = existingAttachments.filter { it.id !in requestIds }
        val createRequests = attachmentRequests.filter { it.id.isNullOrEmpty() }
        val updateRequests = attachmentRequests.filter { !existingIds.contains(it.id) }

        attachmentCommandService.deleteAttachments(toDelete.map { it.id })
        attachmentCommandService.createBulkAttachment(
            AttachmentTargetType.ENTITY_RESUME,
            targetId,
            storagePath,
            createRequests,
        )
        attachmentCommandService.updateBulkAttachment(targetId, storagePath, updateRequests)
    }

    private fun updateLanguageSkills(
        resumeId: String,
        languageSkillRequests: List<ResumeUpdateRequest.LanguageSkillRequest>,
    ) {
        val existingLanguageSkills = languageSkillService.listLanguageSkills(resumeId)
        val requestIds = languageSkillRequests.mapNotNull { it.id }.toSet()

        val toDelete = existingLanguageSkills.filter { it.id !in requestIds }
        val createRequests = languageSkillRequests.filter { it.id.isNullOrEmpty() }
        val updateRequests = languageSkillRequests.filter { !it.id.isNullOrEmpty() }

        languageSkillService.deleteLanguageSkills(toDelete.map { it.id })
        val createdLanguageSkills = languageSkillService.createBulkLanguageSkill(resumeId, createRequests)
        val updatedLanguageSkills = languageSkillService.updateBulkLanguageSkill(resumeId, updateRequests)

        // 생성된 LanguageSkill의 LanguageTest 처리
        createdLanguageSkills.forEachIndexed { index, languageSkill ->
            val request = languageSkillRequests.filter { it.id.isNullOrEmpty() }[index]
            if (request.languageTestsList.isNotEmpty()) {
                languageTestService.createBulkLanguageTest(languageSkill.id, request.languageTestsList)
            }
        }

        // 업데이트된 LanguageSkill의 LanguageTest 처리
        updatedLanguageSkills.forEach { languageSkill ->
            val request = languageSkillRequests.find { it.id == languageSkill.id } ?: return@forEach
            val existingLanguageTests = languageSkill.languageTests
            val requestTestIds = request.languageTestsList.mapNotNull { it.id }.toSet()

            val testsToDelete = existingLanguageTests.filter { it.id !in requestTestIds }
            val createTestRequests = request.languageTestsList.filter { it.id.isNullOrEmpty() }
            val updateTestRequests = request.languageTestsList.filter { !it.id.isNullOrEmpty() }

            languageTestService.deleteLanguageTests(testsToDelete.map { it.id })
            if (createTestRequests.isNotEmpty()) {
                languageTestService.createBulkLanguageTest(languageSkill.id, createTestRequests)
            }
            if (updateTestRequests.isNotEmpty()) {
                languageTestService.updateBulkLanguageTest(languageSkill.id, updateTestRequests)
            }
        }
    }
}
