package com.spectrum.workfolio.config.service.oauth

/**
 * OAuth 관련 예외 클래스들
 */
class OAuthProviderNotSupportedException(providerName: String) :
    RuntimeException("지원하지 않는 OAuth 제공자입니다: $providerName")

class OAuthUserInfoExtractionException(providerName: String, cause: Throwable? = null) :
    RuntimeException("OAuth 사용자 정보 추출 중 오류가 발생했습니다: provider=$providerName", cause)

class UserRegistrationException(providerId: String, cause: Throwable? = null) :
    RuntimeException("사용자 등록 중 오류가 발생했습니다: providerId=$providerId", cause)
