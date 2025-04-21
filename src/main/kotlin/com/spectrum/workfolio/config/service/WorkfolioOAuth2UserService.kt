package com.spectrum.workfolio.config.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.spectrum.workfolio.domain.dto.PrincipalDetails
import com.spectrum.workfolio.domain.entity.primary.Account
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.model.AccountType
import com.spectrum.workfolio.domain.model.KakaoAccount
import com.spectrum.workfolio.domain.model.SNSType
import com.spectrum.workfolio.services.AccountService
import com.spectrum.workfolio.services.WorkerService
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

/**
 * KAKAO 로그인시 LoginSuccessHandler 보다 먼저 실행됨
 */

@Service
class WorkfolioOAuth2UserService(
    private val workerService: WorkerService,
    private val accountService: AccountService,
) : OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private val objectMapper = jacksonObjectMapper()

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        val delegate = DefaultOAuth2UserService()
        val oAuth2User = delegate.loadUser(userRequest)

        val registrationId = userRequest.clientRegistration.registrationId

        when (registrationId.uppercase()) {
            SNSType.KAKAO.name -> handleKakaoLogin(oAuth2User)
            else -> throw IllegalStateException("Unsupported OAuth2 provider: $registrationId")
        }

        return PrincipalDetails(registrationId, oAuth2User)
    }

    private fun handleKakaoLogin(oauth2User: OAuth2User) {
        val kakaoAccountMap = oauth2User.attributes["kakao_account"] as Map<*, *>

        val kakaoAccount = objectMapper.convertValue(kakaoAccountMap, KakaoAccount::class.java)
        val profile = kakaoAccount.profile
        val providerId = oauth2User.attributes["id"].toString()

        val account = accountService.getAccountByProviderId(providerId)

        if(account.isEmpty){
            val worker = workerService.createWorker(
                Worker(
                    name = profile.nickname,
                )
            )

            accountService.createAccount(
                Account(
                    type = AccountType.KAKAO,
                    providerId = providerId,
                    worker = worker
                )
            )
        }
    }
}
