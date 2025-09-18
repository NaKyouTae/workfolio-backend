package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.dto.ExistingData
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.entity.history.Company
import com.spectrum.workfolio.domain.entity.history.Position
import com.spectrum.workfolio.domain.entity.history.Salary
import com.spectrum.workfolio.domain.entity.primary.Certifications
import com.spectrum.workfolio.domain.entity.primary.Degrees
import com.spectrum.workfolio.domain.entity.primary.Education
import com.spectrum.workfolio.domain.repository.CertificationsRepository
import com.spectrum.workfolio.domain.repository.CompanyRepository
import com.spectrum.workfolio.domain.repository.DegreesRepository
import com.spectrum.workfolio.domain.repository.EducationRepository
import com.spectrum.workfolio.domain.repository.PositionRepository
import com.spectrum.workfolio.domain.repository.SalaryRepository
import com.spectrum.workfolio.proto.worker_career.WorkerCareerUpdateRequest
import com.spectrum.workfolio.utils.TimeUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class WorkerCareerService(
    private val workerService: WorkerService,
    private val salaryRepository: SalaryRepository,
    private val degreesRepository: DegreesRepository,
    private val companyRepository: CompanyRepository,
    private val positionRepository: PositionRepository,
    private val educationRepository: EducationRepository,
    private val certificationsRepository: CertificationsRepository,
) {

    @Transactional
    fun updateCareer(workerId: String, request: WorkerCareerUpdateRequest) {
        val worker = workerService.getWorker(workerId)

        // 한 번에 모든 기존 데이터 조회
        val existingData = loadExistingData(workerId)

        createOrUpdateCompany(worker, request.companiesList, existingData.companies)
        createOrUpdateCertifications(worker, request.certificationsList, existingData.certifications)
        createOrUpdateDegrees(worker, request.degreesList, existingData.degrees)
        createOrUpdateEducations(worker, request.educationsList, existingData.educations)
    }

    private fun loadExistingData(workerId: String): ExistingData {
        val companies = companyRepository.findByWorkerId(workerId)
        val companyIds = companies.map { it.id }

        val positions = positionRepository.findPositionsByCompanyIds(companyIds)
        val salaries = salaryRepository.findSalariesByCompanyIds(companyIds)

        // Company에 Position, Salary 연결
        companies.forEach { company ->
            positions.filter { it.company.id == company.id }.forEach { company.addPosition(it) }
            salaries.filter { it.company.id == company.id }.forEach { company.addSalary(it) }
        }
        val certifications = certificationsRepository.findByWorkerId(workerId)
        val degrees = degreesRepository.findByWorkerId(workerId)
        val educations = educationRepository.findByWorkerId(workerId)

        return ExistingData(companies, certifications, degrees, educations)
    }

    private fun updateCompany(existingCompany: Company, companyReq: WorkerCareerUpdateRequest.WorkerCompany) {
        // 회사 정보만 업데이트
        val startedAt = TimeUtil.ofEpochMilli(companyReq.startedAt).toLocalDate()
        val endedAt = calEndedAt(companyReq.endedAt)?.toLocalDate()
        val isWorking = companyReq.isWorking

        existingCompany.changeInfo(startedAt, endedAt, isWorking)

        // Position, Salary는 기존 것과 비교하여 변경된 것만 처리
        updatePositions(existingCompany, companyReq.positionsList)
        updateSalaries(existingCompany, companyReq.salariesList)
    }

    private fun updatePositions(company: Company, newPositions: List<WorkerCareerUpdateRequest.WorkerCompany.WorkerCompanyPosition>) {
        val existingPositions = company.positions.associateBy { it.name }
        val newPositionNames = newPositions.map { it.name }.toSet()

        // 삭제할 Position 찾기
        val toDelete = existingPositions.keys - newPositionNames
        toDelete.forEach { name ->
            existingPositions[name]?.let { positionRepository.delete(it) }
        }

        // 새로 추가하거나 업데이트할 Position 처리
        newPositions.forEach { positionReq ->
            val existing = existingPositions[positionReq.name]
            if (existing != null) {
                // 업데이트
                val startedAt = TimeUtil.ofEpochMilli(positionReq.startedAt).toLocalDate()
                val endedAt = calEndedAt(positionReq.endedAt)?.toLocalDate()

                existing.changeInfo(startedAt, endedAt)
            } else {
                // 새로 생성
                val position = Position(
                    name = positionReq.name,
                    startedAt = TimeUtil.ofEpochMilli(positionReq.startedAt).toLocalDate(),
                    endedAt = calEndedAt(positionReq.endedAt)?.toLocalDate(),
                    company = company,
                )
                company.addPosition(position)
            }
        }
    }

    private fun updateSalaries(company: Company, newSalaries: List<WorkerCareerUpdateRequest.WorkerCompany.WorkerCompanySalary>) {
        val existingSalaries = company.salaries.associateBy { it.amount.toString() + "_" + it.startedAt.toString() }
        val newSalaryKeys = newSalaries.map { it.amount.toString() + "_" + TimeUtil.ofEpochMilli(it.startedAt).toLocalDate().toString() }.toSet()

        // 삭제할 Salary 찾기
        val toDelete = existingSalaries.keys - newSalaryKeys
        toDelete.forEach { key ->
            existingSalaries[key]?.let { salaryRepository.delete(it) }
        }

        // 새로 추가하거나 업데이트할 Salary 처리
        newSalaries.forEach { salaryReq ->
            val key = salaryReq.amount.toString() + "_" + TimeUtil.ofEpochMilli(salaryReq.startedAt).toLocalDate().toString()
            val existing = existingSalaries[key]
            if (existing != null) {
                // 업데이트
                val amount = salaryReq.amount
                val startedAt = TimeUtil.ofEpochMilli(salaryReq.startedAt).toLocalDate()
                val endedAt = calEndedAt(salaryReq.endedAt)?.toLocalDate()

                existing.changeInfo(amount, startedAt, endedAt)
            } else {
                // 새로 생성
                val salary = Salary(
                    amount = salaryReq.amount,
                    startedAt = TimeUtil.ofEpochMilli(salaryReq.startedAt).toLocalDate(),
                    endedAt = calEndedAt(salaryReq.endedAt)?.toLocalDate(),
                    company = company,
                )
                company.addSalary(salary)
            }
        }
    }

    private fun createOrUpdateCompany(worker: Worker, companiesList: List<WorkerCareerUpdateRequest.WorkerCompany>, existingCompanies: List<Company>) {
        val existingCompanyMap = existingCompanies.associateBy { it.name }

        companiesList.forEach { companyReq ->
            val existingCompany = existingCompanyMap[companyReq.name]

            if (existingCompany != null) {
                updateCompany(existingCompany, companyReq)
            } else {
                createNewCompany(worker, companyReq)
            }
        }
    }

    private fun createOrUpdateCertifications(worker: Worker, certificationsList: List<WorkerCareerUpdateRequest.WorkerCertifications>, existingCertifications: List<Certifications>) {
        val existingMap = existingCertifications.associateBy { it.name }

        certificationsList.forEach { req ->
            val existing = existingMap[req.name]

            if (existing != null) {
                updateCertification(existing, req)
            } else {
                createNewCertification(worker, req)
            }
        }
    }

    private fun createOrUpdateDegrees(worker: Worker, degreesList: List<WorkerCareerUpdateRequest.WorkerDegrees>, existingDegrees: List<Degrees>) {
        val existingMap = existingDegrees.associateBy { it.name }

        degreesList.forEach { req ->
            val existing = existingMap[req.name]

            if (existing != null) {
                updateDegree(existing, req)
            } else {
                createNewDegree(worker, req)
            }
        }
    }

    private fun createOrUpdateEducations(worker: Worker, educationsList: List<WorkerCareerUpdateRequest.WorkerEducation>, existingEducations: List<Education>) {
        val existingMap = existingEducations.associateBy { it.name }

        educationsList.forEach { req ->
            val existing = existingMap[req.name]

            if (existing != null) {
                updateEducation(existing, req)
            } else {
                createNewEducation(worker, req)
            }
        }
    }

    private fun createNewCompany(worker: Worker, companyReq: WorkerCareerUpdateRequest.WorkerCompany) {
        val company = Company(
            name = companyReq.name,
            startedAt = TimeUtil.ofEpochMilli(companyReq.startedAt).toLocalDate(),
            endedAt = calEndedAt(companyReq.endedAt)?.toLocalDate(),
            isWorking = companyReq.isWorking,
            worker = worker,
        )

        // Position들 생성 및 추가
        companyReq.positionsList.forEach { positionReq ->
            val position = Position(
                name = positionReq.name,
                startedAt = TimeUtil.ofEpochMilli(positionReq.startedAt).toLocalDate(),
                endedAt = calEndedAt(positionReq.endedAt)?.toLocalDate(),
                company = company,
            )
            company.addPosition(position)
        }

        // Salary들 생성 및 추가
        companyReq.salariesList.forEach { salaryReq ->
            val salary = Salary(
                amount = salaryReq.amount,
                startedAt = TimeUtil.ofEpochMilli(salaryReq.startedAt).toLocalDate(),
                endedAt = calEndedAt(salaryReq.endedAt)?.toLocalDate(),
                company = company,
            )
            company.addSalary(salary)
        }

        companyRepository.save(company)
    }

    private fun createNewCertification(worker: Worker, req: WorkerCareerUpdateRequest.WorkerCertifications) {
        val certification = Certifications(
            name = req.name,
            number = req.number,
            issuer = req.issuer,
            issuedAt = TimeUtil.ofEpochMilli(req.issuedAt).toLocalDate(),
            expirationPeriod = calEndedAt(req.expirationPeriod)?.toLocalDate(),
            worker = worker,
        )
        certificationsRepository.save(certification)
    }

    private fun updateCertification(existing: Certifications, req: WorkerCareerUpdateRequest.WorkerCertifications) {
        val number = req.number
        val issuer = req.issuer
        val issuedAt = TimeUtil.ofEpochMilli(req.issuedAt).toLocalDate()
        val expirationPeriod = calEndedAt(req.expirationPeriod)?.toLocalDate()
        existing.changeInfo(number, issuer, issuedAt, expirationPeriod)
        certificationsRepository.save(existing)
    }

    private fun createNewDegree(worker: Worker, req: WorkerCareerUpdateRequest.WorkerDegrees) {
        val degree = Degrees(
            name = req.name,
            major = req.major,
            startedAt = TimeUtil.ofEpochMilli(req.startedAt).toLocalDate(),
            endedAt = calEndedAt(req.endedAt)?.toLocalDate(),
            worker = worker,
        )
        degreesRepository.save(degree)
    }

    private fun updateDegree(existing: Degrees, req: WorkerCareerUpdateRequest.WorkerDegrees) {
        val major = req.major
        val startedAt = TimeUtil.ofEpochMilli(req.startedAt).toLocalDate()
        val endedAt = calEndedAt(req.endedAt)?.toLocalDate()
        existing.changeInfo(major, startedAt, endedAt)
        degreesRepository.save(existing)
    }

    private fun createNewEducation(worker: Worker, req: WorkerCareerUpdateRequest.WorkerEducation) {
        val education = Education(
            name = req.name,
            startedAt = TimeUtil.ofEpochMilli(req.startedAt).toLocalDate(),
            endedAt = calEndedAt(req.endedAt)?.toLocalDate(),
            agency = req.agency,
            worker = worker,
        )
        educationRepository.save(education)
    }

    private fun updateEducation(existing: Education, req: WorkerCareerUpdateRequest.WorkerEducation) {
        val startedAt = TimeUtil.ofEpochMilli(req.startedAt).toLocalDate()
        val endedAt = calEndedAt(req.endedAt)?.toLocalDate()
        val agency = req.agency
        existing.changeInfo(startedAt, endedAt, agency)
        educationRepository.save(existing)
    }

    private fun calEndedAt(endedAt: Long): LocalDateTime? {
        return if (endedAt != 0L) {
            TimeUtil.ofEpochMilli(endedAt)
        } else null
    }
}
