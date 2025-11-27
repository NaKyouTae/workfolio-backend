package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.staff.StaffChangePasswordRequest
import com.spectrum.workfolio.proto.staff.StaffCreateRequest
import com.spectrum.workfolio.proto.staff.StaffGetResponse
import com.spectrum.workfolio.proto.staff.StaffListResponse
import com.spectrum.workfolio.proto.staff.StaffLoginRequest
import com.spectrum.workfolio.proto.staff.StaffLoginResponse
import com.spectrum.workfolio.proto.staff.StaffUpdateRequest
import com.spectrum.workfolio.services.staff.StaffAuthService
import com.spectrum.workfolio.services.staff.StaffCommandService
import com.spectrum.workfolio.services.staff.StaffQueryService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/staffs")
class StaffController(
    private val staffQueryService: StaffQueryService,
    private val staffCommandService: StaffCommandService,
    private val staffAuthService: StaffAuthService,
) {

    @PostMapping("/login")
    fun login(
        @RequestBody request: StaffLoginRequest,
    ): StaffLoginResponse {
        return staffAuthService.login(request)
    }

    @GetMapping
    fun listStaffs(): StaffListResponse {
        return staffQueryService.listStaffsResult()
    }

    @GetMapping("/{id}")
    fun getStaff(
        @PathVariable id: String,
    ): StaffGetResponse {
        return staffQueryService.getStaffResult(id)
    }

    @PostMapping
    fun createStaff(
        @RequestBody request: StaffCreateRequest,
    ): SuccessResponse {
        staffCommandService.createStaff(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping
    fun updateStaff(
        @RequestBody request: StaffUpdateRequest,
    ): SuccessResponse {
        staffCommandService.updateStaff(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @PutMapping("/password")
    fun changePassword(
        @RequestBody request: StaffChangePasswordRequest,
    ): SuccessResponse {
        staffCommandService.changePassword(request)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @DeleteMapping("/{id}")
    fun deleteStaff(
        @PathVariable id: String,
    ): SuccessResponse {
        staffCommandService.deleteStaff(id)
        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
