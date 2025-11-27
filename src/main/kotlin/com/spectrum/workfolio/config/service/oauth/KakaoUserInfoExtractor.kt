package com.spectrum.workfolio.config.service.oauth

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.spectrum.workfolio.domain.enums.KakaoAccount
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Component

/**
 * 카카오 OAuth 사용자 정보 추출기
 */
@Component
class KakaoUserInfoExtractor : OAuthUserInfoExtractor {

    private val logger = LoggerFactory.getLogger(KakaoUserInfoExtractor::class.java)
    private val objectMapper = jacksonObjectMapper()

    override fun extractUserInfo(oauth2User: OAuth2User): OAuthUserInfo {
        return try {
            val kakaoAccountMap = oauth2User.attributes["kakao_account"] as? Map<*, *>
                ?: throw OAuthUserInfoExtractionException("KAKAO", IllegalArgumentException("kakao_account 정보가 없습니다"))

            val kakaoAccount = objectMapper.convertValue(kakaoAccountMap, KakaoAccount::class.java)
            val profile = kakaoAccount.profile
            val providerId = oauth2User.attributes["id"]?.toString()
                ?: throw OAuthUserInfoExtractionException("KAKAO", IllegalArgumentException("provider ID가 없습니다"))

            OAuthUserInfo(
                providerId = providerId,
                nickName = profile.nickname,
                email = kakaoAccount.email ?: "",
                phoneNumber = kakaoAccount.getNormalizedPhoneNumber(),
                gender = kakaoAccount.getGender(),
                birthDate = kakaoAccount.getBirthDate(),
                profileImageUrl = profile.profileImageUrl,
            )
        } catch (e: Exception) {
            logger.error("카카오 사용자 정보 추출 중 오류 발생", e)
            throw OAuthUserInfoExtractionException("KAKAO", e)
        }
    }
}
