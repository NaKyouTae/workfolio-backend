package com.spectrum.workfolio.config.service.oauth

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

/**
 * 카카오 API 호출을 담당하는 Provider
 */
@Component
class KakaoApiProvider(
    private val restTemplate: RestTemplate,
) {

    private val logger = LoggerFactory.getLogger(KakaoApiProvider::class.java)

    @Value("\${kakao.api.base-url:https://kapi.kakao.com}")
    private lateinit var kakaoApiBaseUrl: String

    @Value("\${kakao.api.admin-key}")
    private lateinit var adminKey: String

    /**
     * 카카오 연결 해제 (회원탈퇴) - Admin Key 사용
     * @param kakaoUserId 카카오 사용자 ID
     * @return 연결 해제 성공 여부
     */
    fun unlinkUser(kakaoUserId: String): Boolean {
        return try {
            logger.info("카카오 연결 해제 요청 시작: userId={}", kakaoUserId)

            val url = "$kakaoApiBaseUrl/v1/user/unlink"

            val headers = HttpHeaders().apply {
                set("Authorization", "KakaoAK $adminKey")
                contentType = MediaType.APPLICATION_FORM_URLENCODED
            }

            val body = LinkedMultiValueMap<String, String>().apply {
                add("target_id_type", "user_id")
                add("target_id", kakaoUserId)
            }

            val request = HttpEntity(body, headers)

            val response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map::class.java,
            )

            logger.info("카카오 연결 해제 성공: {}", response.statusCode)
            response.statusCode.is2xxSuccessful
        } catch (e: Exception) {
            logger.error("카카오 연결 해제 실패: userId={}", kakaoUserId, e)
            false
        }
    }

    /**
     * 카카오 로그아웃
     * @param accessToken 카카오 액세스 토큰
     * @return 로그아웃 성공 여부
     */
    fun logout(accessToken: String): Boolean {
        return try {
            logger.info("카카오 로그아웃 요청 시작")

            val url = "$kakaoApiBaseUrl/v1/user/logout"

            val headers = HttpHeaders().apply {
                set("Authorization", "Bearer $accessToken")
                contentType = MediaType.APPLICATION_FORM_URLENCODED
            }

            val body = LinkedMultiValueMap<String, String>()
            val request = HttpEntity(body, headers)

            val response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                Map::class.java,
            )

            logger.info("카카오 로그아웃 성공: {}", response.statusCode)
            response.statusCode.is2xxSuccessful
        } catch (e: Exception) {
            logger.error("카카오 로그아웃 실패", e)
            false
        }
    }
}
