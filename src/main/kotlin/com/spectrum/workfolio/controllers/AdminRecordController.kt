package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.record.AdminRecordListResponse
import com.spectrum.workfolio.services.record.AdminRecordService
import com.spectrum.workfolio.services.record.RecordCommandService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/records")
class AdminRecordController(
    private val adminRecordService: AdminRecordService,
    private val recordCommandService: RecordCommandService,
) {

    @GetMapping
    fun getRecordsByWorkerId(
        @RequestParam workerId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
    ): AdminRecordListResponse {
        return adminRecordService.getRecordsByWorkerId(workerId, page, size)
    }

    @DeleteMapping("/{id}")
    fun deleteRecord(
        @PathVariable id: String,
        @RequestParam workerId: String,
    ): SuccessResponse {
        recordCommandService.deleteRecord(workerId, id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
