package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.turn_over.CheckListCheckedUpdateRequest
import com.spectrum.workfolio.proto.turn_over.CheckListResponse
import com.spectrum.workfolio.services.turnovers.CheckListService
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/check-lists")
class CheckListController(
    private val checkListService: CheckListService,
) {

    @PutMapping("/checked")
    fun updateChecked(
        @RequestBody request: CheckListCheckedUpdateRequest,
    ): CheckListResponse {
        return checkListService.updateChecked(request)
    }
}

