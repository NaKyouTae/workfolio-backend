package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.domain.extensions.toProtoResponse
import com.spectrum.workfolio.proto.record_group.CreateRecordGroupRequest
import com.spectrum.workfolio.proto.record_group.CreateRecordGroupResponse
import com.spectrum.workfolio.proto.record_group.JoinRecordGroupRequest
import com.spectrum.workfolio.proto.record_group.JoinRecordGroupResponse
import com.spectrum.workfolio.proto.record_group.ListRecordGroupResponse
import com.spectrum.workfolio.proto.record_group.UpdateRecordGroupRequest
import com.spectrum.workfolio.proto.record_group.UpdateRecordGroupResponse
import com.spectrum.workfolio.services.RecordGroupService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/record-groups")
class RecordGroupController(
    private val recordGroupService: RecordGroupService,
) {

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun listRecordGroup(@AuthenticatedUser workerId: String): ListRecordGroupResponse {
        val recordGroups = recordGroupService.listRecordGroups(workerId)
        val groups = recordGroups.map { it.toProtoResponse() }

        return ListRecordGroupResponse.newBuilder().addAllGroups(groups).build()
    }

    @PostMapping
    fun createRecordGroup(
        @AuthenticatedUser workerId: String,
        @RequestBody request: CreateRecordGroupRequest
    ): CreateRecordGroupResponse {
        val recordGroup = recordGroupService.createRecordGroup(workerId, request)

        return recordGroup.toProtoResponse()
    }

    @PostMapping("/join")
    fun joinRecordGroup(
        @AuthenticatedUser workerId: String,
        @RequestBody request: JoinRecordGroupRequest
    ): JoinRecordGroupResponse {
        recordGroupService.joinRecordGroup(workerId, request)
        return JoinRecordGroupResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping("/{recordGroupId}")
    fun updateRecordGroup(
        @AuthenticatedUser workerId: String,
        @PathVariable recordGroupId: String,
        @RequestBody request: UpdateRecordGroupRequest
    ): UpdateRecordGroupResponse {
        recordGroupService.updateRecordGroup(workerId, recordGroupId, request)

        return UpdateRecordGroupResponse.newBuilder().setIsSuccess(true).build()
    }
}
