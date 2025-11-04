package com.spectrum.workfolio.services.resume

import com.spectrum.workfolio.domain.entity.resume.Project
import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.ProjectRepository
import com.spectrum.workfolio.proto.resume.ResumeUpdateRequest
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val resumeQueryService: ResumeQueryService,
) {

    @Transactional(readOnly = true)
    fun getProject(id: String): Project {
        return projectRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_PROJECT.message) }
    }

    @Transactional(readOnly = true)
    fun listProjects(resumeId: String): List<Project> {
        return projectRepository.findByResumeIdOrderByPriorityAsc(resumeId)
    }

    @Transactional
    fun createProject(
        resumeId: String,
        title: String? = null,
        affiliation: String? = null,
        role: String? = null,
        description: String? = null,
        startedAt: Long? = null,
        endedAt: Long? = null,
        isVisible: Boolean,
        priority: Int = 0,
    ): Project {
        val resume = resumeQueryService.getResume(resumeId)
        val project = Project(
            title = title ?: "",
            role = role ?: "",
            affiliation = affiliation ?: "",
            description = description ?: "",
            startedAt = TimeUtil.ofEpochMilliNullable(startedAt)?.toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(endedAt)?.toLocalDate(),
            isVisible = isVisible,
            priority = priority,
            resume = resume,
        )

        return projectRepository.save(project)
    }

    @Transactional
    fun createBulkProjectFromEntity(
        resume: Resume,
        projects: List<Project>,
    ) {
        val newProjects = projects.map {
            Project(
                title = it.title,
                role = it.role,
                affiliation = it.affiliation,
                description = it.description,
                startedAt = it.startedAt,
                endedAt = it.endedAt,
                isVisible = it.isVisible,
                priority = it.priority,
                resume = resume,
            )
        }

        projectRepository.saveAll(newProjects)
    }

    @Transactional
    fun updateProject(
        id: String,
        title: String? = null,
        affiliation: String? = null,
        role: String? = null,
        description: String? = null,
        startedAt: Long? = null,
        endedAt: Long? = null,
        isVisible: Boolean,
        priority: Int = 0,
    ): Project {
        val project = this.getProject(id)

        project.changeInfo(
            title = title ?: "",
            role = role ?: "",
            affiliation = affiliation ?: "",
            description = description ?: "",
            startedAt = TimeUtil.ofEpochMilliNullable(startedAt)?.toLocalDate(),
            endedAt = TimeUtil.ofEpochMilliNullable(endedAt)?.toLocalDate(),
            isVisible = isVisible,
            priority = priority,
        )

        return projectRepository.save(project)
    }

    @Transactional
    fun createBulkProject(
        resumeId: String,
        requests: List<ResumeUpdateRequest.ProjectRequest>,
    ): List<Project> {
        val resume = resumeQueryService.getResume(resumeId)
        val entities = requests.map { request ->
            Project(
                title = request.title,
                role = request.role,
                affiliation = request.affiliation,
                description = request.description,
                startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
                endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
                isVisible = request.isVisible,
                priority = request.priority,
                resume = resume,
            )
        }

        return projectRepository.saveAll(entities)
    }

    @Transactional
    fun updateBulkProject(
        resumeId: String,
        requests: List<ResumeUpdateRequest.ProjectRequest>,
    ): List<Project> {
        val existingProjects = projectRepository.findByResumeIdOrderByPriorityAsc(resumeId)

        val requestMap = requests.associateBy { it.id }

        val updatedEntities = existingProjects.mapNotNull { entity ->
            requestMap[entity.id]?.let { request ->
                entity.changeInfo(
                    title = request.title,
                    role = request.role,
                    affiliation = request.affiliation,
                    description = request.description,
                    startedAt = TimeUtil.ofEpochMilliNullable(request.startedAt)?.toLocalDate(),
                    endedAt = TimeUtil.ofEpochMilliNullable(request.endedAt)?.toLocalDate(),
                    isVisible = request.isVisible,
                    priority = request.priority,
                )
                entity
            }
        }

        return projectRepository.saveAll(updatedEntities)
    }

    @Transactional
    fun deleteProject(id: String) {
        val project = this.getProject(id)
        projectRepository.delete(project)
    }

    @Transactional
    fun deleteProjects(projectIds: List<String>) {
        if (projectIds.isNotEmpty()) {
            projectRepository.deleteAllById(projectIds)
        }
    }

    @Transactional
    fun deleteProjectsByResumeId(resumeId: String) {
        val projects = projectRepository.findByResumeIdOrderByPriorityAsc(resumeId)
        projectRepository.deleteAll(projects)
    }
}
