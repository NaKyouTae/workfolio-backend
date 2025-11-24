package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.record_group.CreateRecordGroupRequest
import com.spectrum.workfolio.proto.record_group.RecordGroupDetailResponse
import com.spectrum.workfolio.proto.record_group.RecordGroupJoinRequest
import com.spectrum.workfolio.proto.record_group.RecordGroupListResponse
import com.spectrum.workfolio.proto.record_group.RecordGroupPriorityUpdateRequest
import com.spectrum.workfolio.proto.record_group.RecordGroupResponse
import com.spectrum.workfolio.proto.record_group.RecordGroupUpdateRequest
import com.spectrum.workfolio.services.record.RecordGroupService
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
    fun listOwnedRecordGroup(@AuthenticatedUser workerId: String): RecordGroupListResponse {
        val recordGroups = recordGroupService.listOwnedRecordGroups(workerId)
        return RecordGroupListResponse.newBuilder().addAllGroups(recordGroups).build()
    }

    @GetMapping("/shared")
    fun listSharedRecordGroup(@AuthenticatedUser workerId: String): RecordGroupListResponse {
        val recordGroups = recordGroupService.listSharedRecordGroups(workerId)
        return RecordGroupListResponse.newBuilder().addAllGroups(recordGroups).build()
    }

    @GetMapping("/editable")
    fun listEditableRecordGroup(@AuthenticatedUser workerId: String): RecordGroupListResponse {
        val recordGroups = recordGroupService.listEditableRecordGroups(workerId)
        return RecordGroupListResponse.newBuilder().addAllGroups(recordGroups).build()
    }

    @GetMapping("/details/{id}")
    fun listWorkerRecordGroupDetail(@PathVariable id: String): RecordGroupDetailResponse {
        return recordGroupService.listRecordGroupDetailResult(id)
    }

    @PostMapping
    fun createRecordGroup(
        @AuthenticatedUser workerId: String,
        @RequestBody request: CreateRecordGroupRequest,
    ): RecordGroupResponse {
        return recordGroupService.createRecordGroup(workerId, false, request)
    }

    @PostMapping("/join")
    fun joinRecordGroup(
        @AuthenticatedUser workerId: String,
        @RequestBody request: RecordGroupJoinRequest,
    ): RecordGroupResponse {
        return recordGroupService.joinRecordGroup(workerId, request)
    }

    @PutMapping("/{recordGroupId}")
    fun updateRecordGroup(
        @PathVariable recordGroupId: String,
        @RequestBody request: RecordGroupUpdateRequest,
    ): RecordGroupResponse {
        return recordGroupService.updateRecordGroup(recordGroupId, request)
    }

    @PutMapping("/priorities")
    fun updatePriorities(
        @AuthenticatedUser workerId: String,
        @RequestBody request: RecordGroupPriorityUpdateRequest,
    ): SuccessResponse {
        return recordGroupService.updatePriorities(workerId, request)
    }

    @DeleteMapping("/{recordGroupId}")
    fun deleteRecordGroup(
        @AuthenticatedUser workerId: String,
        @PathVariable recordGroupId: String,
    ): SuccessResponse {
        recordGroupService.deleteRecordGroup(workerId, recordGroupId)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
