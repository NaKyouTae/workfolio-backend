package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.record.ListRecordResponse
import com.spectrum.workfolio.proto.record.RecordCreateRequest
import com.spectrum.workfolio.proto.record.RecordResponse
import com.spectrum.workfolio.proto.record.RecordUpdateRequest
import com.spectrum.workfolio.services.RecordService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/records")
class RecordController(
    private val recordService: RecordService,
) {

    @GetMapping("/{id}")
    fun getRecord(
        @PathVariable id: String,
    ): RecordResponse {
        return recordService.getRecord(id)
    }

    @GetMapping("/monthly")
    fun listMonthlyRecord(
        @RequestParam year: Int,
        @RequestParam month: Int,
        @RequestParam recordGroupIds: List<String>,
    ): ListRecordResponse {
        return recordService.listMonthlyRecord(year, month, recordGroupIds)
    }

    @GetMapping("/weekly")
    fun listWeeklyRecord(
        @RequestParam year: Int,
        @RequestParam month: Int,
        @RequestParam week: Int,
        @RequestParam recordGroupIds: List<String>,
    ): ListRecordResponse {
        return recordService.listWeeklyRecord(year, month, week, recordGroupIds)
    }

    @PostMapping
    fun createRecord(
        @AuthenticatedUser workerId: String,
        @RequestBody request: RecordCreateRequest,
    ): RecordResponse {
        return recordService.createRecord(workerId, request)
    }

    @PutMapping
    fun updateRecord(
        @RequestBody request: RecordUpdateRequest,
    ): RecordResponse {
        return recordService.updateRecord(request)
    }

    @DeleteMapping("{recordId}")
    fun deleteRecord(
        @AuthenticatedUser workerId: String,
        @PathVariable recordId: String,
    ): SuccessResponse {
        recordService.deleteRecord(workerId, recordId)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
