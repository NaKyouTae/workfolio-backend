package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.Certifications
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.CertificationsRepository
import com.spectrum.workfolio.proto.certifications.CertificationsCreateRequest
import com.spectrum.workfolio.proto.certifications.CertificationsListResponse
import com.spectrum.workfolio.proto.certifications.CertificationsResponse
import com.spectrum.workfolio.proto.certifications.CertificationsUpdateRequest
import com.spectrum.workfolio.proto.common.resume
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CertificationsService(
    private val resumeQueryService: ResumeQueryService,
    private val certificationsRepository: CertificationsRepository,
) {

    @Transactional(readOnly = true)
    fun getCertifications(id: String): Certifications {
        return certificationsRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_CERTIFICATIONS.message) }
    }

    @Transactional(readOnly = true)
    fun listCertifications(resumeId: String): CertificationsListResponse {
        val resume = resumeQueryService.getResume(resumeId)
        val certifications = certificationsRepository.findByResumeIdOrderByIssuedAtDesc(resume.id)
        return CertificationsListResponse.newBuilder()
            .addAllCertifications(certifications.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createCertifications(request: CertificationsCreateRequest): CertificationsResponse {
        val resume = resumeQueryService.getResume(request.resumeId)
        val certifications = Certifications(
            name = request.name,
            number = request.number,
            issuer = request.issuer,
            issuedAt = TimeUtil.ofEpochMilli(request.issuedAt).toLocalDate(),
            expirationPeriod = TimeUtil.ofEpochMilliNullable(request.expirationPeriod)?.toLocalDate(),
            resume = resume,
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

    @Transactional
    fun deleteCertifications(id: String) {
        val certifications = this.getCertifications(id)
        certificationsRepository.delete(certifications)
    }

    @Transactional
    fun deleteCertificationsByResumeId(resumeId: String) {
        val certifications = certificationsRepository.findByResumeIdOrderByIssuedAtDesc(resumeId)
        certificationsRepository.deleteAll(certifications)
    }
}
