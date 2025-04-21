package com.spectrum.workfolio.config.handler

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException

@Component
class WorkfolioOAuth2LogoutSuccessHandler: LogoutSuccessHandler {

    @Value("\${token.httpOnly}")
    private lateinit var httpOnly: String
    @Value("\${token.secure}")
    private lateinit var tokenSecure: String

    @Throws(IOException::class)
    override fun onLogoutSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication?
    ) {
        println("Logout success === 1")
        removeTokenCookies(response)

        println("Logout success === 2")

        val redirectUrl = buildRedirectUrl() // URL 리디렉션 (쿼리 파라미터 전달 유지)
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.writer.write("""{"message": "Logout successful"}""")
        response.status = HttpServletResponse.SC_OK
    }

    private fun removeTokenCookies(response: HttpServletResponse) {
        val accessTokenCookie = ResponseCookie.from("accessToken", "")
            .httpOnly(httpOnly.toBoolean())
            .secure(tokenSecure.toBoolean())
            .path("/")
            .domain("localhost") // 로컬 개발 환경일 경우 생략해도 OK
            .maxAge(0)
            .sameSite("Lax") // optional
            .build()

        val refreshTokenCookie = ResponseCookie.from("refreshToken", "")
            .httpOnly(httpOnly.toBoolean())
            .secure(tokenSecure.toBoolean())
            .path("/")
            .domain("localhost")
            .maxAge(0)
            .sameSite("Lax")
            .build()

        response.addHeader("Set-Cookie", accessTokenCookie.toString())
        response.addHeader("Set-Cookie", refreshTokenCookie.toString())
    }

    private fun buildRedirectUrl(): String {
        return UriComponentsBuilder.fromUriString("http://localhost:3000/login").toUriString()
    }
}
