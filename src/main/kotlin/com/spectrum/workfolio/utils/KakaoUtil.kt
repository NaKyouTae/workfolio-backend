package com.spectrum.workfolio.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.spectrum.workfolio.domain.model.KakaoDTO
import com.spectrum.workfolio.domain.model.KakaoTokenResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

@Component
class KakaoUtil(
    private val restTemplate: RestTemplate
) {
    @Value("\${spring.security.oauth2.client.registration.kakao.client-id}")
    private lateinit var clientId: String

    @Value("\${spring.security.oauth2.client.registration.kakao.client-secret}")
    private lateinit var clientSecret: String

    @Value("\${spring.security.oauth2.client.registration.kakao.redirect-uri}")
    private lateinit var redirectUri: String
    
    @Value("\${kakao.api.admin-key}")
    private lateinit var adminKey: String

    val objectMapper = jacksonObjectMapper()

    fun getKakaoToken(authCode: String): KakaoTokenResponse? {
        val requestBody: MultiValueMap<String, String> = LinkedMultiValueMap()
        requestBody.add("grant_type", "authorization_code")
        requestBody.add("client_id", clientId)
        requestBody.add("client_secret", clientSecret)
        requestBody.add("redirect_uri", redirectUri)
        requestBody.add("code", authCode)

        val headers = HttpHeaders().apply {
            add("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
        }

        val requestEntity = HttpEntity(requestBody, headers)

        val responseEntity = restTemplate.exchange(
            "https://kauth.kakao.com/oauth/token",
            HttpMethod.POST,
            requestEntity,
            KakaoTokenResponse::class.java
        )

        return responseEntity.body
    }

    fun getKakaoProfile(token: String): KakaoDTO {
        val headers = HttpHeaders().apply {
            add("Content-type", "application/x-www-form-urlencoded;charset=utf-8")
            add("Authorization", "Bearer $token")
        }

        val kakaoProfileRequest = HttpEntity<MultiValueMap<String, String>>(headers)

        val response: ResponseEntity<String> = restTemplate.exchange(
            "https://kapi.kakao.com/v2/user/me",
            HttpMethod.GET,
            kakaoProfileRequest,
            String::class.java
        )

        return objectMapper.readValue(response.body, KakaoDTO::class.java)
    }

    fun logout(logoutRedirectUri: String, state: String? = null): Boolean {
        val params = LinkedMultiValueMap<String, String>().apply {
            add("client_id", clientId)
            add("logout_redirect_uri", "http://localhost:3000")
        }

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_FORM_URLENCODED
        }

        val requestEntity = HttpEntity(params, headers)

        try {
            val responseEntity = restTemplate.exchange(
                "https://kauth.kakao.com/oauth/logout",
                HttpMethod.GET,  // 로그아웃 API는 GET 요청 사용
                requestEntity,
                String::class.java
            )

            println("Response: ${responseEntity.body}")

            return responseEntity.statusCode == HttpStatus.FOUND // 302 응답 (리다이렉트)
        } catch (ex: HttpStatusCodeException) {
            println("Error response body: ${ex.responseBodyAsString}")
            println("Error status code: ${ex.statusCode}")
            println("Error message: ${ex.message}")
        } catch (ex: Exception) {
            println("Unexpected error: ${ex.message}")
        }

        return false
    }
}
