package com.spectrum.workfolio.services.staff

import com.spectrum.workfolio.config.provider.JwtTokenProvider
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.StaffRepository
import com.spectrum.workfolio.proto.staff.StaffLoginRequest
import com.spectrum.workfolio.proto.staff.StaffLoginResponse
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Staff 인증 서비스
 */
@Service
@Transactional(readOnly = true)
class StaffAuthService(
    private val staffRepository: StaffRepository,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
) {

    fun login(request: StaffLoginRequest): StaffLoginResponse {
        // Staff 조회
        val staff = staffRepository.findByUsername(request.username)
            ?: throw WorkfolioException(MsgKOR.INVALID_STAFF_CREDENTIALS.message)

        // 비활성 상태 체크
        if (!staff.isActive) {
            throw WorkfolioException(MsgKOR.INACTIVE_STAFF.message)
        }

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.password, staff.password)) {
            throw WorkfolioException(MsgKOR.INVALID_STAFF_CREDENTIALS.message)
        }

        // JWT 토큰 생성
        val tokens = jwtTokenProvider.generateTokenForStaff(staff.id)

        return StaffLoginResponse.newBuilder()
            .setStaff(staff.toProto())
            .setAccessToken(tokens.accessToken)
            .setRefreshToken(tokens.refreshToken)
            .build()
    }
}

