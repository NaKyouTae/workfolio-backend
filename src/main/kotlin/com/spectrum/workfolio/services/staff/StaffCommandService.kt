package com.spectrum.workfolio.services.staff

import com.spectrum.workfolio.domain.entity.Staff
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.StaffRepository
import com.spectrum.workfolio.proto.staff.StaffChangePasswordRequest
import com.spectrum.workfolio.proto.staff.StaffCreateRequest
import com.spectrum.workfolio.proto.staff.StaffUpdateRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Staff 명령 전용 서비스
 */
@Service
@Transactional
class StaffCommandService(
    private val staffRepository: StaffRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
) {

    fun createStaff(request: StaffCreateRequest): Staff {
        // 중복 체크
        if (staffRepository.existsByUsername(request.username)) {
            throw WorkfolioException(MsgKOR.ALREADY_EXISTS_STAFF_USERNAME.message)
        }
        if (staffRepository.existsByEmail(request.email)) {
            throw WorkfolioException(MsgKOR.ALREADY_EXISTS_STAFF_EMAIL.message)
        }

        val staff = Staff(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            name = request.name,
            email = request.email,
            phone = if (request.hasPhone()) request.phone else null,
        )
        return staffRepository.save(staff)
    }

    fun updateStaff(request: StaffUpdateRequest): Staff {
        val staff = staffRepository.findById(request.id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_STAFF.message) }

        // 이메일 중복 체크 (자신 제외)
        staffRepository.findByEmail(request.email)?.let {
            if (it.id != staff.id) {
                throw WorkfolioException(MsgKOR.ALREADY_EXISTS_STAFF_EMAIL.message)
            }
        }

        staff.changeInfo(
            name = request.name,
            email = request.email,
            phone = if (request.hasPhone()) request.phone else null,
        )

        if (request.hasIsActive()) {
            staff.changeActive(request.isActive)
        }

        return staffRepository.save(staff)
    }

    fun changePassword(request: StaffChangePasswordRequest): Staff {
        val staff = staffRepository.findById(request.id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_STAFF.message) }

        // 기존 비밀번호 확인
        if (!passwordEncoder.matches(request.oldPassword, staff.password)) {
            throw WorkfolioException(MsgKOR.INVALID_OLD_PASSWORD.message)
        }

        staff.changePassword(passwordEncoder.encode(request.newPassword))
        return staffRepository.save(staff)
    }

    fun deleteStaff(id: String) {
        val staff = staffRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_STAFF.message) }

        staffRepository.delete(staff)
    }
}

