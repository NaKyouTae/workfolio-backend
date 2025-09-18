package com.spectrum.workfolio.config.service.oauth

import com.spectrum.workfolio.domain.model.SNSType
import org.springframework.stereotype.Component

/**
 * OAuth 제공자별 사용자 정보 추출기 팩토리
 */
@Component
class OAuthUserInfoExtractorFactory(
    private val kakaoUserInfoExtractor: KakaoUserInfoExtractor,
    private val googleUserInfoExtractor: GoogleUserInfoExtractor,
    private val naverUserInfoExtractor: NaverUserInfoExtractor,
) {

    /**
     * 제공자 타입에 따라 적절한 추출기를 반환합니다.
     */
    fun getExtractor(snsType: SNSType): OAuthUserInfoExtractor {
        return when (snsType) {
            SNSType.KAKAO -> kakaoUserInfoExtractor
            SNSType.GOOGLE -> googleUserInfoExtractor
            SNSType.NAVER -> naverUserInfoExtractor
        }
    }

    /**
     * 제공자 이름으로 추출기를 반환합니다.
     */
    fun getExtractorByProviderName(providerName: String): OAuthUserInfoExtractor {
        val snsType = try {
            SNSType.valueOf(providerName.uppercase())
        } catch (e: IllegalArgumentException) {
            throw OAuthProviderNotSupportedException(providerName)
        }

        return getExtractor(snsType)
    }
}
