package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.domain.extensions.toProtoResponse
import com.spectrum.workfolio.proto.CreateRecordGroupRequest
import com.spectrum.workfolio.proto.CreateRecordGroupResponse
import com.spectrum.workfolio.proto.ListRecordGroupResponse
import com.spectrum.workfolio.services.RecordGroupService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/recordGroups")
class RecordGroupController(
    private val recordGroupService: RecordGroupService,
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun list(@AuthenticatedUser workerId: String): ListRecordGroupResponse {
        val recordGroups = recordGroupService.listRecordGroups(workerId)
        val groups = recordGroups.map { it.toProtoResponse() }

        return ListRecordGroupResponse.newBuilder().addAllGroups(groups).build()
    }

    @PostMapping
    fun create(
        @AuthenticatedUser workerId: String,
        @RequestBody params: CreateRecordGroupRequest
    ): CreateRecordGroupResponse {
        val recordGroup = recordGroupService.createRecordGroup(workerId, params)

        return recordGroup.toProtoResponse()
    }
}
