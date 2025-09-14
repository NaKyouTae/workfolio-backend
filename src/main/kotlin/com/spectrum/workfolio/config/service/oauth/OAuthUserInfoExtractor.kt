package com.spectrum.workfolio.config.service.oauth

import org.springframework.security.oauth2.core.user.OAuth2User

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
    val name: String,
    val email: String? = null,
    val profileImageUrl: String? = null
)
