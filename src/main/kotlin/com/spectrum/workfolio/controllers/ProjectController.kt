package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.project.ProjectCreateRequest
import com.spectrum.workfolio.proto.project.ProjectListResponse
import com.spectrum.workfolio.proto.project.ProjectUpdateRequest
import com.spectrum.workfolio.services.ProjectService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/projects")
class ProjectController(
    private val projectService: ProjectService,
) {

    @GetMapping
    fun listProjects(
        @AuthenticatedUser workerId: String,
    ): ProjectListResponse {
        return projectService.listProjects(workerId)
    }

    @PostMapping
    fun createProject(
        @RequestBody request: ProjectCreateRequest,
    ): SuccessResponse {
        projectService.createProject(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updateCompany(
        @RequestBody request: ProjectUpdateRequest,
    ): SuccessResponse {
        projectService.updateProject(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
