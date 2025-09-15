package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.record_group.JoinRecordGroupRequest
import com.spectrum.workfolio.proto.record_group.JoinRecordGroupResponse
import com.spectrum.workfolio.services.WorkerRecordGroupService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/worker-record-groups")
class WorkerRecordGroupController(
    private val workerRecordGroupService: WorkerRecordGroupService,
) {
    @PostMapping("/join")
    fun joinRecordGroup(
        @AuthenticatedUser workerId: String,
        @RequestBody request: JoinRecordGroupRequest
    ): JoinRecordGroupResponse {
        workerRecordGroupService.joinRecordGroup(workerId, request)
        return JoinRecordGroupResponse.newBuilder().setIsSuccess(true).build()
    }
}