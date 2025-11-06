package com.spectrum.workfolio.services.resume

import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toDetailProto
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.ResumeRepository
import com.spectrum.workfolio.proto.resume.ResumeDetailListResponse
import com.spectrum.workfolio.proto.resume.ResumeListResponse
import com.spectrum.workfolio.services.AttachmentQueryService
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Resume 조회 전용 서비스
 * 다른 도메인에서 Resume 정보가 필요할 때 사용
 */
@Service
@Transactional(readOnly = true)
class ResumeQueryService(
    private val resumeRepository: ResumeRepository,
    private val attachmentQueryService: AttachmentQueryService,
) {

    fun getResume(id: String): Resume {
        return resumeRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_RESUME.message) }
    }

    fun getResumeOptional(id: String): Resume? {
        return resumeRepository.findById(id).orElse(null)
    }

    fun getResumes(workerId: String): List<Resume> {
        return resumeRepository.findByWorkerIdOrderByIsDefaultDescUpdatedAtDesc(workerId)
    }

    fun listResumesResult(workerId: String): ResumeListResponse {
        val resumes = resumeRepository.findByWorkerIdOrderByIsDefaultDescUpdatedAtDesc(workerId)
        return ResumeListResponse.newBuilder()
            .addAllResumes(resumes.map { it.toProto() })
            .build()
    }

    fun listResumeDetailsResult(workerId: String): ResumeDetailListResponse {
        val resumes = resumeRepository.findByWorkerIdOrderByIsDefaultDescUpdatedAtDesc(workerId)
        val resumeIds = resumes.map { it.id }
        val attachments = attachmentQueryService.listAttachments(resumeIds)

        return ResumeDetailListResponse.newBuilder()
            .addAllResumes(resumes.map { it.toDetailProto(attachments) })
            .build()
    }
}
