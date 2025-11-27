package com.spectrum.workfolio.config.service

import com.spectrum.workfolio.domain.dto.CustomStaffDetails
import com.spectrum.workfolio.domain.enums.ErrorCode
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.StaffRepository
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class StaffDetailService(
    private val staffRepository: StaffRepository,
) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(id: String): UserDetails {
        return try {
            val staff = staffRepository.findById(id)
                .orElseThrow { UsernameNotFoundException(MsgKOR.NOT_FOUND_STAFF.message) }
            CustomStaffDetails(staff)
        } catch (e: UsernameNotFoundException) {
            throw WorkfolioException(MsgKOR.NOT_FOUND_STAFF.message, ErrorCode.SIGN)
        }
    }
}
