package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toDetailProto
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.ResumeRepository
import com.spectrum.workfolio.proto.resume.ResumeDetailListResponse
import com.spectrum.workfolio.proto.resume.ResumeListResponse
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Resume 조회 전용 서비스
 * 다른 도메인에서 Resume 정보가 필요할 때 사용
 */
@Service
class ResumeQueryService(
    private val resumeRepository: ResumeRepository,
) {

    @Transactional(readOnly = true)
    fun getResume(id: String): Resume {
        return resumeRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_RESUME.message) }
    }

    @Transactional(readOnly = true)
    fun listResumesResult(workerId: String): ResumeListResponse {
        val resumes = resumeRepository.findByWorkerId(workerId)
        return ResumeListResponse.newBuilder()
            .addAllResumes(resumes.map { it.toProto() })
            .build()
    }

    @Transactional(readOnly = true)
    fun listResumeDetailsResult(workerId: String): ResumeDetailListResponse {
        val resumes = resumeRepository.findByWorkerId(workerId)
        return ResumeDetailListResponse.newBuilder()
            .addAllResumes(resumes.map { it.toDetailProto() })
            .build()
    }
}
