package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.Gender
import com.spectrum.workfolio.domain.repository.ResumeRepository
import com.spectrum.workfolio.proto.resume.ResumeCreateRequest
import com.spectrum.workfolio.proto.resume.ResumeUpdateRequest
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
    private val attachmentService: AttachmentService,
    private val resumeQueryService: ResumeQueryService,
    private val languageTestService: LanguageTestService,
    private val languageSkillService: LanguageSkillService,
) {

    fun createResume(workerId: String, request: ResumeCreateRequest): Resume {
        val worker = workerService.getWorker(workerId)
        val resume = Resume(
            title = request.title,
            name = request.name,
            phone = request.phone,
            email = request.email,
            job = request.job,
            gender = convertProtoEnumSafe<Gender>(request.gender),
            birthDate = if (request.hasBirthDate()) TimeUtil.ofEpochMilli(request.birthDate).toLocalDate() else null,
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
            job = originalResume.job,
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
        projectService.createBulkProject(savedResume, originalResume.projects)

        // 3-4. Activity 복제
        activityService.createBulkActivity(savedResume, originalResume.activities)

        // 3-5. LanguageSkill 복제 (LanguageTest 포함)
        originalResume.languageSkills.forEach {
            val savedLanguageSkill = languageSkillService.createLanguageSkill(savedResume, it)
            // LanguageTest 복제
            languageTestService.createBulkLanguageTest(savedLanguageSkill, it.languageTests)
        }

        // 3-6. Attachment 복제
        attachmentService.createBulkAttachment(savedResume, originalResume.attachments)

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
                job = request.job,
                description = request.description,
                birthDate = if (request.hasBirthDate()) TimeUtil.ofEpochMilli(request.birthDate).toLocalDate() else null,
                gender = convertProtoEnumSafe<Gender>(request.gender),
                isPublic = request.isPublic,
                isDefault = request.isDefault,
            )

            resume
        } else {
            val resumeCreateRequest = ResumeCreateRequest.newBuilder()
                .setTitle(request.title)
                .setName(request.name)
                .setPhone(request.phone)
                .setEmail(request.email)
                .setJob(request.job)
                .setGender(request.gender)
                .setBirthDate(request.birthDate)
                .setDescription(request.description)
                .setIsPublic(request.isPublic)
                .setIsDefault(request.isDefault)
                .build()

            this.createResume(workerId, resumeCreateRequest)
        }
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
        educationService.deleteEducations(toDelete.map { it.id })

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
                        .setDescription(request.description)
                        .setStartedAt(request.startedAt)
                        .setEndedAt(request.endedAt)
                        .setIsVisible(request.isVisible)
                        .setPriority(request.priority)
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
                        .setDescription(request.description)
                        .setStartedAt(request.startedAt)
                        .setEndedAt(request.endedAt)
                        .setIsVisible(request.isVisible)
                        .setPriority(request.priority)
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
        val requestIds = careerRequests.filter { it.career.id.isNotEmpty() }.mapNotNull { it.career.id }.toSet()

        // 삭제할 careers
        val toDelete = existingCareers.filter { it.id !in requestIds }
        careerService.deleteCareers(toDelete.map { it.id })

        // 생성 및 수정할 careers
        careerRequests.forEach { request ->
            val career = request.career
            val careerEntity = if (career.id.isNullOrEmpty()) {
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
                        .setDescription(career.description)
                        .setSalary(career.salary)
                        .setStartedAt(career.startedAt)
                        .setEndedAt(career.endedAt)
                        .setIsWorking(career.isWorking)
                        .setIsVisible(career.isVisible)
                        .setPriority(career.priority)
                        .build(),
                )
                createdCareer
            } else {
                // 수정
                val updatedCareer = careerService.updateCareer(
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
                        .setDescription(career.description)
                        .setSalary(career.salary)
                        .setStartedAt(career.startedAt)
                        .setEndedAt(career.endedAt)
                        .setIsWorking(career.isWorking)
                        .setIsVisible(career.isVisible)
                        .setPriority(career.priority)
                        .build(),
                )
                updatedCareer
            }

            // Salaries 처리
            val existingSalaries = careerEntity.salaries
            val requestSalaryIds = request.salariesList.mapNotNull { it.id }.filter { it.isNotBlank() }.toSet()

            // 삭제할 salaries (request에 없는 것들)
            // orphanRemoval = true 설정으로 컬렉션에서 제거만 하면 자동으로 DB에서도 삭제됨
            val salariesToDelete = existingSalaries.filter { it.id !in requestSalaryIds }
            if (salariesToDelete.isNotEmpty()) {
                careerEntity.removeSalaries(salariesToDelete)
            }

            // 생성 및 수정할 salaries
            request.salariesList.forEach { salaryRequest ->
                val negotiationDate = if (salaryRequest.hasNegotiationDate() && salaryRequest.negotiationDate != 0L) {
                    TimeUtil.ofEpochMilli(salaryRequest.negotiationDate).toLocalDate()
                } else {
                    null
                }

                if (salaryRequest.id.isNullOrEmpty()) {
                    // 생성
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
                    // 수정
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
        val existingIds = existingProjects.map { it.id }.toSet()
        val requestIds = projectRequests.mapNotNull { it.id }.toSet()

        // 삭제할 projects
        val toDelete = existingProjects.filter { it.id !in requestIds }
        projectService.deleteProjects(toDelete.map { it.id })

        // 생성 및 수정할 projects
        projectRequests.forEach { request ->
            if (request.id.isNullOrEmpty()) {
                // 생성
                projectService.createProject(
                    resumeId = resumeId,
                    title = request.title,
                    role = request.role,
                    affiliation = request.affiliation,
                    description = request.description,
                    startedAt = request.startedAt,
                    endedAt = request.endedAt,
                    isVisible = request.isVisible,
                    priority = request.priority,
                )
            } else {
                // 수정
                projectService.updateProject(
                    id = request.id,
                    title = request.title,
                    role = request.role,
                    affiliation = request.affiliation,
                    description = request.description,
                    startedAt = request.startedAt,
                    endedAt = request.endedAt,
                    isVisible = request.isVisible,
                    priority = request.priority,
                )
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
        activityService.deleteActivities(toDelete.map { it.id })

        // 생성 및 수정할 activities
        activityRequests.forEach { request ->
            if (request.id.isNullOrEmpty()) {
                // 생성
                activityService.createActivity(
                    resumeId = resumeId,
                    type = convertProtoEnumSafe<com.spectrum.workfolio.domain.enums.ActivityType>(request.type),
                    name = request.name,
                    organization = request.organization,
                    certificateNumber = request.certificateNumber,
                    startedAt = request.startedAt,
                    endedAt = request.endedAt,
                    description = request.description,
                    isVisible = request.isVisible,
                    priority = request.priority,
                )
            } else {
                // 수정
                activityService.updateActivity(
                    id = request.id,
                    type = convertProtoEnumSafe<com.spectrum.workfolio.domain.enums.ActivityType>(request.type),
                    name = request.name,
                    organization = request.organization,
                    certificateNumber = request.certificateNumber,
                    startedAt = request.startedAt,
                    endedAt = request.endedAt,
                    description = request.description,
                    isVisible = request.isVisible,
                    priority = request.priority,
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
        attachmentService.deleteAttachments(toDelete.map { it.id })

        // 생성 및 수정할 attachments
        attachmentRequests.forEach { request ->
            if (request.id.isNullOrEmpty()) {
                // 생성
                attachmentService.createAttachment(
                    resumeId = resumeId,
                    type = convertProtoEnumSafe<com.spectrum.workfolio.domain.enums.AttachmentType>(request.type),
                    category = com.spectrum.workfolio.domain.enums.AttachmentCategory.valueOf(request.category.name),
                    fileName = request.fileName,
                    fileUrl = request.fileUrl,
                    url = request.url,
                    fileData = if (request.hasFileData()) request.fileData else null,
                    isVisible = request.isVisible,
                    priority = request.priority,
                    storagePath = "resumes/attachments/$resumeId"
                )
            } else {
                // 수정
                attachmentService.updateAttachment(
                    id = request.id,
                    type = convertProtoEnumSafe<com.spectrum.workfolio.domain.enums.AttachmentType>(request.type),
                    category = com.spectrum.workfolio.domain.enums.AttachmentCategory.valueOf(request.category.name),
                    fileName = request.fileName,
                    fileUrl = request.fileUrl,
                    url = request.url,
                    fileData = if (request.hasFileData()) request.fileData else null,
                    isVisible = request.isVisible,
                    priority = request.priority,
                    storagePath = "resumes/attachments/${resumeId}"
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
        languageSkillService.deleteLanguageSkills(toDelete.map { it.id })

        // 생성 및 수정할 language skills
        languageSkillRequests.forEach { request ->
            val languageSkillId = if (request.id.isNullOrEmpty()) {
                // 생성
                val createdLanguageSkill = languageSkillService.createLanguageSkill(
                    resumeId = resumeId,
                    language = convertProtoEnumSafe<com.spectrum.workfolio.domain.enums.Language>(request.language),
                    level = convertProtoEnumSafe<com.spectrum.workfolio.domain.enums.LanguageLevel>(request.level),
                    isVisible = request.isVisible,
                    priority = request.priority,
                )
                createdLanguageSkill.id
            } else {
                // 수정
                languageSkillService.updateLanguageSkill(
                    id = request.id,
                    language = convertProtoEnumSafe<com.spectrum.workfolio.domain.enums.Language>(request.language),
                    level = convertProtoEnumSafe<com.spectrum.workfolio.domain.enums.LanguageLevel>(request.level),
                    isVisible = request.isVisible,
                    priority = request.priority,
                )
                request.id
            }

            // 기존 language tests 조회
            val existingLanguageTests = languageSkillService.getLanguageSkill(languageSkillId).languageTests
            val existingTestIds = existingLanguageTests.map { it.id }.toSet()
            val requestTestIds = request.languageTestsList.mapNotNull { it.id }.toSet()

            // 삭제할 language tests
            val testsToDelete = existingLanguageTests.filter { it.id !in requestTestIds }
            languageTestService.deleteLanguageTests(testsToDelete.map { it.id })

            // 생성 및 수정할 language tests
            request.languageTestsList.forEach { testRequest ->
                if (testRequest.id.isNullOrEmpty()) {
                    // 생성
                    languageTestService.createLanguageTest(
                        languageSkillId = languageSkillId,
                        name = testRequest.name,
                        score = testRequest.score,
                        acquiredAt = testRequest.acquiredAt,
                        isVisible = testRequest.isVisible,
                        priority = testRequest.priority,
                    )
                } else {
                    // 수정
                    languageTestService.updateLanguageTest(
                        id = testRequest.id,
                        name = testRequest.name,
                        score = testRequest.score,
                        acquiredAt = testRequest.acquiredAt,
                        isVisible = testRequest.isVisible,
                        priority = testRequest.priority,
                    )
                }
            }
        }
    }
}
