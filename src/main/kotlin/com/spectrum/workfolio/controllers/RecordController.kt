package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.domain.extensions.toProtoResponse
import com.spectrum.workfolio.proto.record.CreateRecordRequest
import com.spectrum.workfolio.proto.record.CreateRecordResponse
import com.spectrum.workfolio.proto.record.ListRecordResponse
import com.spectrum.workfolio.services.RecordService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/records")
class RecordController(
    private val recordService: RecordService,
) {

    @GetMapping
    fun listRecord(@AuthenticatedUser workerId: String): ListRecordResponse {
        return recordService.listProto(workerId)
    }

    @PostMapping
    fun createRecord(
        @AuthenticatedUser workerId: String,
        @RequestBody params: CreateRecordRequest
    ): CreateRecordResponse {
        val record = recordService.create(workerId, params)
        return record.toProtoResponse()
    }
}
