package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.record.ListRecordResponse
import com.spectrum.workfolio.proto.record.RecordCreateRequest
import com.spectrum.workfolio.proto.record.RecordResponse
import com.spectrum.workfolio.proto.record.RecordUpdateRequest
import com.spectrum.workfolio.services.record.RecordCommandService
import com.spectrum.workfolio.services.record.RecordQueryService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/records")
class RecordController(
    private val recordQueryService: RecordQueryService,
    private val recordCommandService: RecordCommandService,
) {

    @GetMapping("/{id}")
    fun getRecord(
        @PathVariable id: String,
    ): RecordResponse {
        return recordQueryService.getRecord(id)
    }

    @GetMapping("/monthly")
    fun listMonthlyRecord(
        @RequestParam year: Int,
        @RequestParam month: Int,
        @RequestParam recordGroupIds: List<String>,
    ): ListRecordResponse {
        return recordQueryService.listMonthlyRecord(year, month, recordGroupIds)
    }

    @GetMapping("/weekly")
    fun listWeeklyRecord(
        @RequestParam startDate: String,
        @RequestParam endDate: String,
        @RequestParam recordGroupIds: List<String>,
    ): ListRecordResponse {
        return recordQueryService.listWeeklyRecord(startDate, endDate, recordGroupIds)
    }

    @PostMapping
    fun createRecord(
        @AuthenticatedUser workerId: String,
        @RequestBody request: RecordCreateRequest,
    ): RecordResponse {
        return recordCommandService.createRecord(workerId, request)
    }

    @PutMapping
    fun updateRecord(
        @RequestBody request: RecordUpdateRequest,
    ): RecordResponse {
        return recordCommandService.updateRecord(request)
    }

    @DeleteMapping("{recordId}")
    fun deleteRecord(
        @AuthenticatedUser workerId: String,
        @PathVariable recordId: String,
    ): SuccessResponse {
        recordCommandService.deleteRecord(workerId, recordId)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
