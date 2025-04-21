package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.domain.extensions.toProtoResponse
import com.spectrum.workfolio.domain.extensions.toRecordProto
import com.spectrum.workfolio.proto.CreateRecordRequest
import com.spectrum.workfolio.proto.CreateRecordResponse
import com.spectrum.workfolio.proto.ListRecordResponse
import com.spectrum.workfolio.services.RecordService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/records")
class RecordController(
    private val recordService: RecordService,
) {

    @GetMapping
    fun list(@AuthenticatedUser workerId: String): ListRecordResponse {
        return recordService.listProto(workerId)
    }

    @PostMapping
    fun create(
        @AuthenticatedUser workerId: String,
        @RequestBody params: CreateRecordRequest
    ): CreateRecordResponse {
        val record = recordService.create(workerId, params)
        return record.toProtoResponse()
    }
}
