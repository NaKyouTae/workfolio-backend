package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.ResumeRepository
import com.spectrum.workfolio.proto.resume.ResumeCreateRequest
import com.spectrum.workfolio.proto.resume.ResumeListResponse
import com.spectrum.workfolio.proto.resume.ResumeUpdateRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ResumeService(
    private val workerService: WorkerService,
    private val resumeRepository: ResumeRepository,
) {

    @Transactional(readOnly = true)
    fun getResume(id: String): Resume {
        return resumeRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_RESUME.message) }
    }

    @Transactional(readOnly = true)
    fun listResumes(workerId: String): ResumeListResponse {
        val resumes = resumeRepository.findByWorkerId(workerId)
        return ResumeListResponse.newBuilder()
            .addAllResumes(resumes.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createResume(workerId: String, request: ResumeCreateRequest): Resume {
        val worker = workerService.getWorker(workerId)
        val resume = Resume(
            title = request.title,
            description = request.description,
            phone = request.phone,
            email = request.email,
            isPublic = request.isPublic,
            isDefault = request.isDefault,
            publicId = request.publicId,
            worker = worker,
        )

        return resumeRepository.save(resume)
    }

    @Transactional
    fun updateResume(workerId: String, request: ResumeUpdateRequest): Resume {
        val resume = this.getResume(request.id)

        resume.changeInfo(
            title = request.title,
            description = request.description,
            phone = request.phone,
            email = request.email,
            isPublic = request.isPublic,
            isDefault = request.isDefault,
            publicId = request.publicId,
        )

        return resumeRepository.save(resume)
    }

    @Transactional
    fun deleteResume(id: String) {
        val resume = this.getResume(id)
        resumeRepository.delete(resume)
    }
}
