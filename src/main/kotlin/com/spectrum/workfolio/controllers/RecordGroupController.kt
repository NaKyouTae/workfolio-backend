package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.domain.extensions.toProtoResponse
import com.spectrum.workfolio.proto.record_group.CreateRecordGroupRequest
import com.spectrum.workfolio.proto.record_group.CreateRecordGroupResponse
import com.spectrum.workfolio.proto.record_group.JoinRecordGroupRequest
import com.spectrum.workfolio.proto.record_group.JoinRecordGroupResponse
import com.spectrum.workfolio.proto.record_group.ListRecordGroupResponse
import com.spectrum.workfolio.proto.record_group.SuccessRecordGroupResponse
import com.spectrum.workfolio.proto.record_group.UpdateRecordGroupRequest
import com.spectrum.workfolio.services.RecordGroupService
import org.springframework.web.bind.annotation.DeleteMapping
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

    @GetMapping("/owned")
    fun listOwnedRecordGroup(@AuthenticatedUser workerId: String): ListRecordGroupResponse {
        val recordGroups = recordGroupService.listOwnedRecordGroups(workerId)
        return ListRecordGroupResponse.newBuilder().addAllGroups(recordGroups).build()
    }

    @GetMapping("/shared")
    fun listSharedRecordGroup(@AuthenticatedUser workerId: String): ListRecordGroupResponse {
        val recordGroups = recordGroupService.listSharedRecordGroups(workerId)
        return ListRecordGroupResponse.newBuilder().addAllGroups(recordGroups).build()
    }

    @GetMapping("/editable")
    fun listEditableRecordGroup(@AuthenticatedUser workerId: String): ListRecordGroupResponse {
        val recordGroups = recordGroupService.listEditableRecordGroups(workerId)
        return ListRecordGroupResponse.newBuilder().addAllGroups(recordGroups).build()
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
    ): SuccessRecordGroupResponse {
        recordGroupService.updateRecordGroup(workerId, recordGroupId, request)

        return SuccessRecordGroupResponse.newBuilder().setIsSuccess(true).build()
    }

    @DeleteMapping("/{recordGroupId}")
    fun deleteRecordGroup(
        @AuthenticatedUser workerId: String,
        @PathVariable recordGroupId: String,
    ): SuccessRecordGroupResponse {
        recordGroupService.deleteRecordGroup(workerId, recordGroupId)

        return SuccessRecordGroupResponse.newBuilder().setIsSuccess(true).build()
    }
}
