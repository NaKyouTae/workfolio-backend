package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.Gender
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.ResumeRepository
import com.spectrum.workfolio.proto.resume.ResumeCreateRequest
import com.spectrum.workfolio.proto.resume.ResumeListResponse
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
    private val resumeRepository: ResumeRepository,
    private val resumeQueryService: ResumeQueryService,
    private val certificationsService: CertificationsService,
    private val degreesService: DegreesService,
    private val educationService: EducationService,
    private val careerService: CareerService,
    private val linkService: LinkService,
) {

    @Transactional
    fun createResume(workerId: String, request: ResumeCreateRequest): Resume {
        val worker = workerService.getWorker(workerId)
        val resume = Resume(
            title = request.title,
            description = "",
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
            description = request.description,
            phone = request.phone,
            email = request.email,
            birthDate = TimeUtil.ofEpochMilli(request.birthDate).toLocalDate(),
            gender = Gender.valueOf(request.gender.name),
            isPublic = request.isPublic,
            isDefault = request.isDefault,
        )

        // Certifications 처리
        updateCertifications(resume.id, request.certificationsList)

        // Degrees 처리
        updateDegrees(resume.id, request.degreesList)

        // Educations 처리
        updateEducations(resume.id, request.educationsList)

        // Careers 처리
        updateCareers(resume.id, request.careersList)

        // Links 처리
        updateLinks(resume.id, request.linksList)

        return resumeRepository.save(resume)
    }

    @Transactional
    fun deleteResume(id: String) {
        val resume = resumeQueryService.getResume(id)
        resumeRepository.delete(resume)
    }

    private fun updateCertifications(
        resumeId: String,
        certificationsRequests: List<ResumeUpdateRequest.CertificationsRequest>,
    ) {
        val existingCertifications = certificationsService.listCertifications(resumeId).certificationsList
        val existingIds = existingCertifications.map { it.id }.toSet()
        val requestIds = certificationsRequests.mapNotNull { it.id }.toSet()

        // 삭제할 certifications (기존에 있지만 요청에 없는 것들)
        val toDelete = existingCertifications.filter { it.id !in requestIds }
        toDelete.forEach { certificationsService.deleteCertifications(it.id) }

        // 생성 및 수정할 certifications
        certificationsRequests.forEach { request ->
            if (request.id.isNullOrEmpty()) {
                // 생성
                certificationsService.createCertifications(
                    com.spectrum.workfolio.proto.certifications.CertificationsCreateRequest.newBuilder()
                        .setResumeId(resumeId)
                        .setName(request.name)
                        .setNumber(request.number)
                        .setIssuer(request.issuer)
                        .setIssuedAt(request.issuedAt)
                        .setExpirationPeriod(request.expirationPeriod)
                        .build(),
                )
            } else {
                // 수정
                certificationsService.updateCertifications(
                    com.spectrum.workfolio.proto.certifications.CertificationsUpdateRequest.newBuilder()
                        .setId(request.id)
                        .setName(request.name)
                        .setNumber(request.number)
                        .setIssuer(request.issuer)
                        .setIssuedAt(request.issuedAt)
                        .setExpirationPeriod(request.expirationPeriod)
                        .build(),
                )
            }
        }
    }

    private fun updateDegrees(
        resumeId: String,
        degreesRequests: List<ResumeUpdateRequest.DegreesRequest>,
    ) {
        val existingDegrees = degreesService.listDegrees(resumeId).degreesList
        val existingIds = existingDegrees.map { it.id }.toSet()
        val requestIds = degreesRequests.mapNotNull { it.id }.toSet()

        // 삭제할 degrees
        val toDelete = existingDegrees.filter { it.id !in requestIds }
        toDelete.forEach { degreesService.deleteDegrees(it.id) }

        // 생성 및 수정할 degrees
        degreesRequests.forEach { request ->
            if (request.id.isNullOrEmpty()) {
                // 생성
                degreesService.createDegrees(
                    com.spectrum.workfolio.proto.degrees.DegreesCreateRequest.newBuilder()
                        .setResumeId(resumeId)
                        .setName(request.name)
                        .setMajor(request.major)
                        .setStatus(request.status)
                        .setStartedAt(request.startedAt)
                        .setEndedAt(request.endedAt)
                        .build(),
                )
            } else {
                // 수정
                degreesService.updateDegrees(
                    com.spectrum.workfolio.proto.degrees.DegreesUpdateRequest.newBuilder()
                        .setId(request.id)
                        .setName(request.name)
                        .setMajor(request.major)
                        .setStatus(request.status)
                        .setStartedAt(request.startedAt)
                        .setEndedAt(request.endedAt)
                        .build(),
                )
            }
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
        toDelete.forEach { educationService.deleteEducation(it.id) }

        // 생성 및 수정할 educations
        educationRequests.forEach { request ->
            if (request.id.isNullOrEmpty()) {
                // 생성
                educationService.createEducation(
                    com.spectrum.workfolio.proto.education.EducationCreateRequest.newBuilder()
                        .setResumeId(resumeId)
                        .setName(request.name)
                        .setStartedAt(request.startedAt)
                        .setEndedAt(request.endedAt)
                        .setAgency(request.agency)
                        .build(),
                )
            } else {
                // 수정
                educationService.updateEducation(
                    com.spectrum.workfolio.proto.education.EducationUpdateRequest.newBuilder()
                        .setId(request.id)
                        .setName(request.name)
                        .setStartedAt(request.startedAt)
                        .setEndedAt(request.endedAt)
                        .setAgency(request.agency)
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
            if (career.id.isNullOrEmpty()) {
                // 생성
                careerService.createCareer(
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
            }
        }
    }

    private fun updateLinks(
        resumeId: String,
        linkRequests: List<ResumeUpdateRequest.LinkRequest>,
    ) {
        val existingLinks = linkService.listLinks(resumeId)
        val existingIds = existingLinks.map { it.id }.toSet()
        val requestIds = linkRequests.mapNotNull { it.id }.toSet()

        // 삭제할 links
        val toDelete = existingLinks.filter { it.id !in requestIds }
        toDelete.forEach { linkService.deleteLink(it.id) }

        // 생성 및 수정할 links
        linkRequests.forEach { request ->
            if (request.id.isNullOrEmpty()) {
                // 생성
                linkService.createLink(resumeId, request.url, request.isVisible)
            } else {
                // 수정
                linkService.updateLink(request.id, request.url, request.isVisible)
            }
        }
    }
}
