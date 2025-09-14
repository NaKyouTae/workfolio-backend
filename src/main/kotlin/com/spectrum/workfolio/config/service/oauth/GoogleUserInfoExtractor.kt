package com.spectrum.workfolio.config.service.oauth

import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component

/**
 * 구글 OAuth 사용자 정보 추출기
 */
@Component
class GoogleUserInfoExtractor : OAuthUserInfoExtractor {
    
    private val logger = LoggerFactory.getLogger(GoogleUserInfoExtractor::class.java)
    
    override fun extractUserInfo(oauth2User: OAuth2User): OAuthUserInfo {
        return try {
            val attributes = oauth2User.attributes
            
            val providerId = attributes["sub"]?.toString()
                ?: throw OAuthUserInfoExtractionException("GOOGLE", IllegalArgumentException("sub 정보가 없습니다"))
            
            val name = attributes["name"]?.toString()
                ?: throw OAuthUserInfoExtractionException("GOOGLE", IllegalArgumentException("name 정보가 없습니다"))
            
            OAuthUserInfo(
                providerId = providerId,
                name = name,
                email = attributes["email"]?.toString(),
                profileImageUrl = attributes["picture"]?.toString()
            )
        } catch (e: Exception) {
            logger.error("구글 사용자 정보 추출 중 오류 발생", e)
            throw OAuthUserInfoExtractionException("GOOGLE", e)
        }
    }
}
