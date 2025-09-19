package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.primary.Certifications
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.CertificationsRepository
import com.spectrum.workfolio.proto.certifications.CertificationsCreateRequest
import com.spectrum.workfolio.proto.certifications.CertificationsListResponse
import com.spectrum.workfolio.proto.certifications.CertificationsResponse
import com.spectrum.workfolio.proto.certifications.CertificationsUpdateRequest
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CertificationsService(
    private val workerService: WorkerService,
    private val certificationsRepository: CertificationsRepository,
) {

    @Transactional(readOnly = true)
    fun getCertifications(id: String): Certifications {
        return certificationsRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_CERTIFICATIONS.message) }
    }

    @Transactional(readOnly = true)
    fun listCertifications(workerId: String): CertificationsListResponse {
        val worker = workerService.getWorker(workerId)
        val educations = certificationsRepository.findByWorkerIdOrderByIssuedAtDesc(worker.id)
        return CertificationsListResponse.newBuilder()
            .addAllCertifications(educations.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createCertifications(workerId: String, request: CertificationsCreateRequest): CertificationsResponse {
        val worker = workerService.getWorker(workerId)
        val certifications = Certifications(
            name = request.name,
            number = request.number,
            issuer = request.issuer,
            issuedAt = TimeUtil.ofEpochMilli(request.issuedAt).toLocalDate(),
            expirationPeriod = TimeUtil.ofEpochMilliNullable(request.expirationPeriod)?.toLocalDate(),
            worker = worker,
        )

        val createdCertifications = certificationsRepository.save(certifications)

        return CertificationsResponse.newBuilder().setCertifications(createdCertifications.toProto()).build()
    }

    @Transactional
    fun updateCertifications(request: CertificationsUpdateRequest): CertificationsResponse {
        val certifications = this.getCertifications(request.id)

        certifications.changeInfo(
            name = request.name,
            number = request.number,
            issuer = request.issuer,
            issuedAt = TimeUtil.ofEpochMilli(request.issuedAt).toLocalDate(),
            expirationPeriod = TimeUtil.ofEpochMilliNullable(request.expirationPeriod)?.toLocalDate(),
        )

        val updatedCertifications = certificationsRepository.save(certifications)

        return CertificationsResponse.newBuilder().setCertifications(updatedCertifications.toProto()).build()
    }
}
