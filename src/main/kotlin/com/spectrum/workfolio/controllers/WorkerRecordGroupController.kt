package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.services.WorkerRecordGroupService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/worker-record-groups")
class WorkerRecordGroupController(
    private val workerRecordGroupService: WorkerRecordGroupService,
) {

    @DeleteMapping
    fun deleteRecordGroup(
        @RequestParam(required = true) recordGroupId: String,
        @RequestParam(required = true) targetWorkerId: String,
    ): SuccessResponse {
        workerRecordGroupService.deleteWorkerRecordGroup(targetWorkerId, recordGroupId)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
