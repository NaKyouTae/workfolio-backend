package com.spectrum.workfolio.config.service.oauth

import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component

/**
 * 네이버 OAuth 사용자 정보 추출기
 */
@Component
class NaverUserInfoExtractor : OAuthUserInfoExtractor {
    
    private val logger = LoggerFactory.getLogger(NaverUserInfoExtractor::class.java)
    
    override fun extractUserInfo(oauth2User: OAuth2User): OAuthUserInfo {
        return try {
            val response = oauth2User.attributes["response"] as? Map<*, *>
                ?: throw OAuthUserInfoExtractionException("NAVER", IllegalArgumentException("response 정보가 없습니다"))
            
            val providerId = response["id"]?.toString()
                ?: throw OAuthUserInfoExtractionException("NAVER", IllegalArgumentException("id 정보가 없습니다"))
            
            val name = response["name"]?.toString()
                ?: throw OAuthUserInfoExtractionException("NAVER", IllegalArgumentException("name 정보가 없습니다"))
            
            OAuthUserInfo(
                providerId = providerId,
                name = name,
                email = response["email"]?.toString(),
                profileImageUrl = response["profile_image"]?.toString()
            )
        } catch (e: Exception) {
            logger.error("네이버 사용자 정보 추출 중 오류 발생", e)
            throw OAuthUserInfoExtractionException("NAVER", e)
        }
    }
}
