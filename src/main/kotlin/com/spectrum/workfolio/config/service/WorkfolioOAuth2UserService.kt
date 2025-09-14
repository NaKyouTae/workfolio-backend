package com.spectrum.workfolio.config.service

import com.spectrum.workfolio.config.service.oauth.OAuthProviderMapper
import com.spectrum.workfolio.config.service.oauth.OAuthUserInfoExtractorFactory
import com.spectrum.workfolio.domain.dto.PrincipalDetails
import com.spectrum.workfolio.services.AccountService
import com.spectrum.workfolio.services.UserRegistrationService
import org.slf4j.LoggerFactory
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

/**
 * OAuth2 사용자 로그인 처리를 담당하는 서비스
 * 다양한 OAuth 제공자(카카오, 구글, 네이버)를 지원합니다.
 */
@Service
class WorkfolioOAuth2UserService(
    private val accountService: AccountService,
    private val userRegistrationService: UserRegistrationService,
    private val oauthUserInfoExtractorFactory: OAuthUserInfoExtractorFactory,
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private val logger = LoggerFactory.getLogger(WorkfolioOAuth2UserService::class.java)

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)
        val registrationId = userRequest.clientRegistration.registrationId

        logger.info("OAuth 로그인 처리 시작: provider={}", registrationId)

        try {
            handleOAuthLogin(oAuth2User, registrationId)
            logger.info("OAuth 로그인 처리 완료: provider={}", registrationId)
        } catch (e: Exception) {
            logger.error("OAuth 로그인 처리 중 오류 발생: provider={}", registrationId, e)
            throw e
        }

        return PrincipalDetails(registrationId, oAuth2User)
    }

    private fun handleOAuthLogin(oauth2User: OAuth2User, providerName: String) {
        // 1. 제공자별 사용자 정보 추출
        val extractor = oauthUserInfoExtractorFactory.getExtractorByProviderName(providerName)
        val oauthUserInfo = extractor.extractUserInfo(oauth2User)
        
        // 2. 기존 계정 확인
        val existingAccount = accountService.getAccountByProviderId(oauthUserInfo.providerId)
        
        if (existingAccount.isEmpty) {
            // 3. 새 사용자 등록
            val accountType = OAuthProviderMapper.toAccountType(providerName)
            userRegistrationService.registerNewUser(oauthUserInfo, accountType)
            logger.info("새 사용자 등록 완료: providerId={}", oauthUserInfo.providerId)
        } else {
            logger.info("기존 사용자 로그인: providerId={}", oauthUserInfo.providerId)
        }
    }
}
