package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.resume.Project
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.ProjectRepository
import com.spectrum.workfolio.proto.project.ProjectCreateRequest
import com.spectrum.workfolio.proto.project.ProjectListResponse
import com.spectrum.workfolio.proto.project.ProjectUpdateRequest
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProjectService(
    private val companyService: CompanyService,
    private val projectRepository: ProjectRepository,
) {

    @Transactional(readOnly = true)
    fun getProject(id: String): Project {
        return projectRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_PROJECT.message) }
    }

    @Transactional(readOnly = true)
    fun listProjects(companyId: String): ProjectListResponse {
        val projects = projectRepository.findByCompanyId(companyId)
        return ProjectListResponse.newBuilder()
            .addAllProjects(projects.map { it.toProto() })
            .build()
    }

    @Transactional
    fun createProject(request: ProjectCreateRequest): Project {
        val company = companyService.getCompany(request.companyId)
        val project = Project(
            title = request.title,
            description = request.description,
            isVisible = request.isVisible,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
            company = company,
        )

        return projectRepository.save(project)
    }

    @Transactional
    fun updateProject(request: ProjectUpdateRequest): Project {
        val project = this.getProject(request.id)

        project.changeInfo(
            title = request.title,
            description = request.description,
            isVisible = request.isVisible,
            startedAt = TimeUtil.ofEpochMilli(request.startedAt).toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
        )

        return projectRepository.save(project)
    }

    @Transactional
    fun deleteProject(id: String) {
        val project = this.getProject(id)
        projectRepository.delete(project)
    }
}
