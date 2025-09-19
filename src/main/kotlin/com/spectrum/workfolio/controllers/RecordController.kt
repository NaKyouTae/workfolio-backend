package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.proto.record.CreateRecordRequest
import com.spectrum.workfolio.proto.record.ListRecordResponse
import com.spectrum.workfolio.proto.record.RecordResponse
import com.spectrum.workfolio.services.RecordService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/records")
class RecordController(
    private val recordService: RecordService,
) {

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
        @RequestBody params: CreateRecordRequest,
    ): RecordResponse {
        return recordService.create(workerId, params)
    }
}
