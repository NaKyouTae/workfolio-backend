package com.spectrum.workfolio.config.service.oauth

import com.spectrum.workfolio.domain.enums.Gender
import org.springframework.security.oauth2.core.user.OAuth2User
import java.time.LocalDate

/**
 * OAuth 제공자별 사용자 정보 추출 인터페이스
 */
interface OAuthUserInfoExtractor {
    fun extractUserInfo(oauth2User: OAuth2User): OAuthUserInfo
}

/**
 * OAuth 사용자 정보를 담는 데이터 클래스
 */
data class OAuthUserInfo(
    val providerId: String,
    val nickName: String,
    val email: String,
    val phoneNumber: String,
    val birthDate: LocalDate? = null,
    val gender: Gender? = null,
    val profileImageUrl: String? = null,
)
