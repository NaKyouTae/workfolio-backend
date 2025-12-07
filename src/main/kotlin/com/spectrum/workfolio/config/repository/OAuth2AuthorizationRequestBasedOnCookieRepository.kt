package com.spectrum.workfolio.config.repository

import com.spectrum.workfolio.utils.CookieUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.stereotype.Component

@Component
class OAuth2AuthorizationRequestBasedOnCookieRepository : AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    companion object {
        private const val OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request"
        private const val COOKIE_EXPIRE_SECONDS = 18000
    }

    override fun saveAuthorizationRequest(
        authorizationRequest: OAuth2AuthorizationRequest?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ) {
        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response)
            return
        }

        // OAuth2AuthorizationRequest를 DTO로 변환하여 저장 (Jackson 역직렬화 문제 해결)
        val dto = toDTO(authorizationRequest)
        CookieUtils.addCookie(
            response,
            OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
            CookieUtils.serialize(dto),
            COOKIE_EXPIRE_SECONDS,
        )
    }

    override fun loadAuthorizationRequest(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        val cookieOpt = CookieUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)

        if (cookieOpt.isEmpty) {
            return null
        }

        val cookie = cookieOpt.get()
        
        // DTO로 역직렬화 후 OAuth2AuthorizationRequest로 복원
        return try {
            val dto = CookieUtils.deserialize(cookie, OAuth2AuthorizationRequestDTO::class.java)
            fromDTO(dto)
        } catch (e: Exception) {
            // 역직렬화 실패 시 null 반환
            null
        }
    }

    /**
     * OAuth2AuthorizationRequest를 DTO로 변환
     */
    private fun toDTO(request: OAuth2AuthorizationRequest): OAuth2AuthorizationRequestDTO {
        return OAuth2AuthorizationRequestDTO(
            authorizationUri = request.authorizationUri,
            clientId = request.clientId,
            redirectUri = request.redirectUri.toString(),
            scopes = request.scopes,
            state = request.state,
            additionalParameters = request.additionalParameters.mapValues { it.value.toString() },
            authorizationRequestUri = request.authorizationRequestUri?.toString(),
            attributes = request.attributes,
        )
    }

    /**
     * DTO를 OAuth2AuthorizationRequest로 복원
     */
    private fun fromDTO(dto: OAuth2AuthorizationRequestDTO): OAuth2AuthorizationRequest {
        return OAuth2AuthorizationRequest.authorizationCode()
            .authorizationUri(dto.authorizationUri)
            .clientId(dto.clientId)
            .redirectUri(dto.redirectUri)
            .scopes(dto.scopes)
            .state(dto.state)
            .additionalParameters(dto.additionalParameters)
            .authorizationRequestUri(dto.authorizationRequestUri)
            .attributes { it.putAll(dto.attributes) }
            .build()
    }

    override fun removeAuthorizationRequest(request: HttpServletRequest, response: HttpServletResponse): OAuth2AuthorizationRequest? {
        val authorizationRequest = loadAuthorizationRequest(request)
        removeAuthorizationRequestCookies(request, response)
        return authorizationRequest
    }

    private fun removeAuthorizationRequestCookies(request: HttpServletRequest, response: HttpServletResponse) {
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
    }
}
