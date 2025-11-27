package com.spectrum.workfolio.services.staff

import com.spectrum.workfolio.domain.entity.Staff
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.StaffRepository
import com.spectrum.workfolio.proto.staff.StaffGetResponse
import com.spectrum.workfolio.proto.staff.StaffListResponse
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Staff 조회 전용 서비스
 */
@Service
@Transactional(readOnly = true)
class StaffQueryService(
    private val staffRepository: StaffRepository,
) {

    fun getStaff(id: String): Staff {
        return staffRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_STAFF.message) }
    }

    fun getStaffByUsername(username: String): Staff {
        return staffRepository.findByUsername(username)
            ?: throw WorkfolioException(MsgKOR.NOT_FOUND_STAFF.message)
    }

    fun getStaffResult(id: String): StaffGetResponse {
        val staff = getStaff(id)
        return StaffGetResponse.newBuilder()
            .setStaff(staff.toProto())
            .build()
    }

    fun listStaffs(): List<Staff> {
        return staffRepository.findAll()
    }

    fun listStaffsResult(): StaffListResponse {
        val staffs = listStaffs()
        return StaffListResponse.newBuilder()
            .addAllStaffs(staffs.map { it.toProto() })
            .build()
    }
}
