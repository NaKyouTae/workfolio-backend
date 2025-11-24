package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.record_group.SharedRecordGroupPriorityUpdateRequest
import com.spectrum.workfolio.proto.record_group.WorkerRecordGroupPriorityUpdateRequest
import com.spectrum.workfolio.services.record.WorkerRecordGroupService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/worker-record-groups")
class WorkerRecordGroupController(
    private val workerRecordGroupService: WorkerRecordGroupService,
) {

    @PutMapping("/priorities")
    fun updatePriorities(
        @AuthenticatedUser workerId: String,
        @RequestBody request: WorkerRecordGroupPriorityUpdateRequest,
    ): SuccessResponse {
        return workerRecordGroupService.updatePriorities(workerId, request)
    }

    @PutMapping("/shared/priorities")
    fun updateSharedRecordGroupPriorities(
        @AuthenticatedUser workerId: String,
        @RequestBody request: SharedRecordGroupPriorityUpdateRequest,
    ): SuccessResponse {
        return workerRecordGroupService.updateSharedRecordGroupPriorities(workerId, request)
    }

    @DeleteMapping
    fun leaveRecordGroup(
        @RequestParam(required = true) recordGroupId: String,
        @RequestParam(required = true) targetWorkerId: String,
    ): SuccessResponse {
        workerRecordGroupService.leaveWorkerRecordGroup(targetWorkerId, recordGroupId)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
