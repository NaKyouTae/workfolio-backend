package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.Gender
import com.spectrum.workfolio.domain.repository.ResumeRepository
import com.spectrum.workfolio.proto.resume.ResumeCreateRequest
import com.spectrum.workfolio.proto.resume.ResumeUpdateRequest
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
    private val activityService: ActivityService,
    private val resumeRepository: ResumeRepository,
    private val educationService: EducationService,
    private val attachmentService: AttachmentService,
    private val resumeQueryService: ResumeQueryService,
    private val languageTestService: LanguageTestService,
    private val languageSkillService: LanguageSkillService,
) {

    @Transactional
    fun createResume(workerId: String, request: ResumeCreateRequest): Resume {
        val worker = workerService.getWorker(workerId)
        val resume = Resume(
            title = request.title,
            name = "",
            phone = "",
            email = "",
            birthDate = TimeUtil.now().toLocalDate(),
            gender = Gender.MALE,
            isPublic = false,
            isDefault = false,
            publicId = Resume.generatePublicId(),
            worker = worker,
        )

        return resumeRepository.save(resume)
    }

    @Transactional
    fun updateResume(request: ResumeUpdateRequest): Resume {
        val resume = resumeQueryService.getResume(request.id)

        // Resume 기본 정보 업데이트
        resume.changeInfo(
            title = request.title,
            name = request.name,
            phone = request.phone,
            email = request.email,
            birthDate = TimeUtil.ofEpochMilli(request.birthDate).toLocalDate(),
            gender = Gender.valueOf(request.gender.name),
            isPublic = request.isPublic,
            isDefault = request.isDefault,
        )

        // 학력 처리
        updateEducations(resume.id, request.educationsList)

        // 경력 처리
        updateCareers(resume.id, request.careersList)

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

    private fun updateEducations(
        resumeId: String,
        educationRequests: List<ResumeUpdateRequest.EducationRequest>,
    ) {
        val existingEducations = educationService.listEducations(resumeId).educationsList
        val existingIds = existingEducations.map { it.id }.toSet()
        val requestIds = educationRequests.mapNotNull { it.id }.toSet()

        // 삭제할 educations
        val toDelete = existingEducations.filter { it.id !in requestIds }
        toDelete.forEach { educationService.deleteEducation(it.id) }

        // 생성 및 수정할 educations
        educationRequests.forEach { request ->
            if (request.id.isNullOrEmpty()) {
                // 생성
                educationService.createEducation(
                    com.spectrum.workfolio.proto.education.EducationCreateRequest.newBuilder()
                        .setResumeId(resumeId)
                        .setStatus(request.status)
                        .setName(request.name)
                        .setMajor(request.major)
                        .setStartedAt(request.startedAt)
                        .setEndedAt(request.endedAt)
                        .setIsVisible(request.isVisible)
                        .build(),
                )
            } else {
                // 수정
                educationService.updateEducation(
                    com.spectrum.workfolio.proto.education.EducationUpdateRequest.newBuilder()
                        .setId(request.id)
                        .setStatus(request.status)
                        .setName(request.name)
                        .setMajor(request.major)
                        .setStartedAt(request.startedAt)
                        .setEndedAt(request.endedAt)
                        .setIsVisible(request.isVisible)
                        .build(),
                )
            }
        }
    }

    private fun updateCareers(
        resumeId: String,
        careerRequests: List<ResumeUpdateRequest.CareerRequest>,
    ) {
        val existingCareers = careerService.listCareers(resumeId).careersList
        val existingIds = existingCareers.map { it.id }.toSet()
        val requestIds = careerRequests.mapNotNull { it.career.id }.toSet()

        // 삭제할 careers
        val toDelete = existingCareers.filter { it.id !in requestIds }
        toDelete.forEach { careerService.deleteCareer(it.id) }

        // 생성 및 수정할 careers
        careerRequests.forEach { request ->
            val career = request.career
            val careerId = if (career.id.isNullOrEmpty()) {
                // 생성
                val createdCareer = careerService.createCareer(
                    com.spectrum.workfolio.proto.career.CareerCreateRequest.newBuilder()
                        .setResumeId(resumeId)
                        .setName(career.name)
                        .setPosition(career.position)
                        .setEmploymentType(
                            com.spectrum.workfolio.proto.common.Career.EmploymentType.valueOf(
                                career.employmentType.name,
                            ),
                        )
                        .setDepartment(career.department)
                        .setJobGrade(career.jobGrade)
                        .setJob(career.job)
                        .setSalary(career.salary)
                        .setStartedAt(career.startedAt)
                        .setEndedAt(career.endedAt)
                        .setIsWorking(career.isWorking)
                        .setIsVisible(career.isVisible)
                        .build(),
                )
                createdCareer.career.id
            } else {
                // 수정
                careerService.updateCareer(
                    com.spectrum.workfolio.proto.career.CareerUpdateRequest.newBuilder()
                        .setId(career.id)
                        .setName(career.name)
                        .setPosition(career.position)
                        .setEmploymentType(
                            com.spectrum.workfolio.proto.common.Career.EmploymentType.valueOf(
                                career.employmentType.name,
                            ),
                        )
                        .setDepartment(career.department)
                        .setJobGrade(career.jobGrade)
                        .setJob(career.job)
                        .setSalary(career.salary)
                        .setStartedAt(career.startedAt)
                        .setEndedAt(career.endedAt)
                        .setIsWorking(career.isWorking)
                        .setIsVisible(career.isVisible)
                        .build(),
                )
                career.id
            }

            // Career 엔티티에 Project와 Salary를 직접 추가 (cascade로 자동 저장)
            val careerEntity = if (career.id.isNullOrEmpty()) {
                // 새로 생성된 career 엔티티 가져오기
                val createdCareer = careerService.createCareer(
                    com.spectrum.workfolio.proto.career.CareerCreateRequest.newBuilder()
                        .setResumeId(resumeId)
                        .setName(career.name)
                        .setPosition(career.position)
                        .setEmploymentType(
                            com.spectrum.workfolio.proto.common.Career.EmploymentType.valueOf(
                                career.employmentType.name,
                            ),
                        )
                        .setDepartment(career.department)
                        .setJobGrade(career.jobGrade)
                        .setJob(career.job)
                        .setSalary(career.salary)
                        .setStartedAt(career.startedAt)
                        .setEndedAt(career.endedAt)
                        .setIsWorking(career.isWorking)
                        .setIsVisible(career.isVisible)
                        .build(),
                )
                careerService.getCareer(createdCareer.career.id)
            } else {
                // 기존 career 엔티티 가져오기
                careerService.getCareer(career.id)
            }

            // Achievement 추가
            request.achievementsList.forEach { projectRequest ->
                val project = com.spectrum.workfolio.domain.entity.resume.Achievement(
                    title = projectRequest.title,
                    description = projectRequest.description,
                    isVisible = projectRequest.isVisible,
                    startedAt = TimeUtil.ofEpochMilli(projectRequest.startedAt).toLocalDate(),
                    endedAt = if (projectRequest.endedAt > 0) TimeUtil.ofEpochMilli(projectRequest.endedAt).toLocalDate() else null,
                    career = careerEntity,
                )
                careerEntity.addAchievement(project)
            }

            // Salaries 추가
            request.salariesList.forEach { salaryRequest ->
                val salary = com.spectrum.workfolio.domain.entity.resume.Salary(
                    amount = salaryRequest.amount,
                    memo = salaryRequest.memo,
                    isVisible = salaryRequest.isVisible,
                    negotiationDate = TimeUtil.ofEpochMilli(salaryRequest.negotiationDate).toLocalDate(),
                    career = careerEntity,
                )
                careerEntity.addSalary(salary)
            }
        }
    }

    private fun updateActivities(
        resumeId: String,
        activityRequests: List<ResumeUpdateRequest.ActivityRequest>,
    ) {
        val existingActivities = activityService.listActivities(resumeId)
        val existingIds = existingActivities.map { it.id }.toSet()
        val requestIds = activityRequests.mapNotNull { it.id }.toSet()

        // 삭제할 activities
        val toDelete = existingActivities.filter { it.id !in requestIds }
        toDelete.forEach { activityService.deleteActivity(it.id) }

        // 생성 및 수정할 activities
        activityRequests.forEach { request ->
            if (request.id.isNullOrEmpty()) {
                // 생성
                activityService.createActivity(
                    resumeId = resumeId,
                    type = if (request.hasType()) com.spectrum.workfolio.domain.enums.ActivityType.valueOf(request.type.name) else null,
                    name = request.name,
                    organization = request.organization,
                    certificateNumber = request.certificateNumber,
                    startedAt = request.startedAt,
                    endedAt = request.endedAt,
                    description = request.description,
                    isVisible = request.isVisible,
                )
            } else {
                // 수정
                activityService.updateActivity(
                    id = request.id,
                    type = if (request.hasType()) com.spectrum.workfolio.domain.enums.ActivityType.valueOf(request.type.name) else null,
                    name = request.name,
                    organization = request.organization,
                    certificateNumber = request.certificateNumber,
                    startedAt = request.startedAt,
                    endedAt = request.endedAt,
                    description = request.description,
                    isVisible = request.isVisible,
                )
            }
        }
    }

    private fun updateAttachments(
        resumeId: String,
        attachmentRequests: List<ResumeUpdateRequest.AttachmentRequest>,
    ) {
        val existingAttachments = attachmentService.listAttachments(resumeId)
        val existingIds = existingAttachments.map { it.id }.toSet()
        val requestIds = attachmentRequests.mapNotNull { it.id }.toSet()

        // 삭제할 attachments
        val toDelete = existingAttachments.filter { it.id !in requestIds }
        toDelete.forEach { attachmentService.deleteAttachment(it.id) }

        // 생성 및 수정할 attachments
        attachmentRequests.forEach { request ->
            if (request.id.isNullOrEmpty()) {
                // 생성
                attachmentService.createAttachment(
                    resumeId = resumeId,
                    type = if (request.hasType()) com.spectrum.workfolio.domain.enums.AttachmentType.valueOf(request.type.name) else null,
                    fileName = request.fileName,
                    fileUrl = request.fileUrl,
                    isVisible = request.isVisible,
                )
            } else {
                // 수정
                attachmentService.updateAttachment(
                    id = request.id,
                    type = if (request.hasType()) com.spectrum.workfolio.domain.enums.AttachmentType.valueOf(request.type.name) else null,
                    fileName = request.fileName,
                    fileUrl = request.fileUrl,
                    isVisible = request.isVisible,
                )
            }
        }
    }

    private fun updateLanguageSkills(
        resumeId: String,
        languageSkillRequests: List<ResumeUpdateRequest.LanguageSkillRequest>,
    ) {
        val existingLanguageSkills = languageSkillService.listLanguageSkills(resumeId)
        val existingIds = existingLanguageSkills.map { it.id }.toSet()
        val requestIds = languageSkillRequests.mapNotNull { it.id }.toSet()

        // 삭제할 language skills
        val toDelete = existingLanguageSkills.filter { it.id !in requestIds }
        toDelete.forEach { languageSkillService.deleteLanguageSkill(it.id) }

        // 생성 및 수정할 language skills
        languageSkillRequests.forEach { request ->
            val languageSkillId = if (request.id.isNullOrEmpty()) {
                // 생성
                val createdLanguageSkill = languageSkillService.createLanguageSkill(
                    resumeId = resumeId,
                    language = if (request.hasLanguage()) com.spectrum.workfolio.domain.enums.Language.valueOf(request.language.name) else null,
                    level = if (request.hasLevel()) com.spectrum.workfolio.domain.enums.LanguageLevel.valueOf(request.level.name) else null,
                    isVisible = request.isVisible,
                )
                createdLanguageSkill.id
            } else {
                // 수정
                languageSkillService.updateLanguageSkill(
                    id = request.id,
                    language = if (request.hasLanguage()) com.spectrum.workfolio.domain.enums.Language.valueOf(request.language.name) else null,
                    level = if (request.hasLevel()) com.spectrum.workfolio.domain.enums.LanguageLevel.valueOf(request.level.name) else null,
                    isVisible = request.isVisible,
                )
                request.id
            }

            // 기존 language tests 조회
            val existingLanguageTests = languageSkillService.getLanguageSkill(languageSkillId).languageTests
            val existingTestIds = existingLanguageTests.map { it.id }.toSet()
            val requestTestIds = request.languageTestsList.mapNotNull { it.id }.toSet()

            // 삭제할 language tests
            val testsToDelete = existingLanguageTests.filter { it.id !in requestTestIds }
            testsToDelete.forEach { languageTestService.deleteLanguageTest(it.id) }

            // 생성 및 수정할 language tests
            request.languageTestsList.forEach { testRequest ->
                if (testRequest.id.isNullOrEmpty()) {
                    // 생성
                    languageTestService.createLanguageTest(
                        languageSkillId = languageSkillId,
                        testName = testRequest.testName,
                        score = testRequest.score,
                        acquiredAt = testRequest.acquiredAt,
                        isVisible = testRequest.isVisible,
                    )
                } else {
                    // 수정
                    languageTestService.updateLanguageTest(
                        id = testRequest.id,
                        testName = testRequest.testName,
                        score = testRequest.score,
                        acquiredAt = testRequest.acquiredAt,
                        isVisible = testRequest.isVisible,
                    )
                }
            }
        }
    }
}
