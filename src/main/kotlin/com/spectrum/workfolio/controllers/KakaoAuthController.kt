package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.utils.KakaoUtil
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class KakaoAuthController(
    private val kakaoUtil: KakaoUtil,
) {
    @Value("\${token.httpOnly}")
    private lateinit var tokenHttpOnly: String

    @Value("\${token.secure}")
    private lateinit var tokenSecure: String

    @GetMapping("/oauth2/kakao")
    fun redirectToKakao(response: HttpServletResponse) {
        response.sendRedirect("/oauth2/authorization/kakao")
    }

    @GetMapping("/logout")
    fun kakaoLogout(
        @RequestHeader("Authorization") token: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<String> {
        val bearerToken = token.removePrefix("Bearer ")

        return if (kakaoUtil.logout(bearerToken)) {
            val session = request.getSession(false) // 기존 세션을 가져오되 없으면 null을 반환

            session?.invalidate() // 세션 무효화

            // 쿠키 만료 처리
            val expiredCookies = listOf(
                Cookie("JSESSIONID", null),
                Cookie("accessToken", null),
                Cookie("refreshToken", null),
                Cookie("oauth2_auth_request", null),
            )

            expiredCookies.forEach { cookie ->
                cookie.apply {
                    maxAge = 0 // 쿠키 만료
                    path = "/" // 모든 경로에서 유효
                    domain = "localhost" // 도메인 설정 (프론트엔드와 서버가 같은 도메인에 있어야 함)
                    isHttpOnly = tokenHttpOnly.toBoolean() // 클라이언트 측 스크립트에서 접근할 수 없도록
                    secure = tokenSecure.toBoolean() // HTTPS가 아니라면 false 설정
                }
                response.addCookie(cookie)
            }

            ResponseEntity.ok("카카오 로그아웃 성공")
        } else {
            ResponseEntity.badRequest().body("카카오 로그아웃 실패")
        }
    }
}
