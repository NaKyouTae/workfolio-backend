package com.spectrum.workfolio.config.service

import com.spectrum.workfolio.domain.dto.CustomUserDetails
import com.spectrum.workfolio.domain.enums.ErrorCode
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.WorkerRepository
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class WorkerDetailService(
    private val workerRepository: WorkerRepository,
) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(id: String): UserDetails {
        return try {
            val worker = workerRepository.findById(id)
                .orElseThrow { UsernameNotFoundException(MsgKOR.NOT_FOUND_WORKER.message) }
            CustomUserDetails(worker)
        } catch (e: UsernameNotFoundException) {
            throw WorkfolioException(MsgKOR.NOT_FOUND_WORKER.message, ErrorCode.SIGN)
        }
    }
}
