package com.spectrum.workfolio.config.service.oauth

import com.spectrum.workfolio.domain.model.AccountType
import com.spectrum.workfolio.domain.model.SNSType

/**
 * OAuth 제공자 이름과 AccountType, SNSType 간의 매핑을 담당하는 유틸리티
 */
object OAuthProviderMapper {
    
    /**
     * 제공자 이름을 SNSType으로 변환합니다.
     */
    fun toSNSType(providerName: String): SNSType {
        return try {
            SNSType.valueOf(providerName.uppercase())
        } catch (e: IllegalArgumentException) {
            throw OAuthProviderNotSupportedException(providerName)
        }
    }
    
    /**
     * 제공자 이름을 AccountType으로 변환합니다.
     */
    fun toAccountType(providerName: String): AccountType {
        return try {
            AccountType.valueOf(providerName.uppercase())
        } catch (e: IllegalArgumentException) {
            throw OAuthProviderNotSupportedException(providerName)
        }
    }
    
    /**
     * SNSType을 AccountType으로 변환합니다.
     */
    fun toAccountType(snsType: SNSType): AccountType {
        return when (snsType) {
            SNSType.KAKAO -> AccountType.KAKAO
            SNSType.GOOGLE -> AccountType.GOOGLE
            SNSType.NAVER -> AccountType.NAVER
        }
    }
}
