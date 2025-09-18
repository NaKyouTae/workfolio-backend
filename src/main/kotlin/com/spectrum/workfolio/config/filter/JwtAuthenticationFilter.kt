package com.spectrum.workfolio.config.filter

import com.spectrum.workfolio.config.exception.JwtAuthenticationException
import com.spectrum.workfolio.config.provider.JwtTokenProvider
import com.spectrum.workfolio.domain.model.MsgKOR
import com.spectrum.workfolio.redis.service.RedisBlackAccessTokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

class JwtAuthenticationFilter(
    private val tokenProvider: JwtTokenProvider,
    private val redisBlackAccessTokenService: RedisBlackAccessTokenService,
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val excludePath = listOf(
            "/api/oauth2",
            "/login",
            "/logout",
            "/error",
            "/favicon.ico",
            "/actuator",
            "/.well-known/appspecific/com.chrome.devtools.json",
        )

        val path = request.requestURI
        return excludePath.any { path.startsWith(it) }
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = resolveToken(request)

        if (token != null) {
            // 블랙 리스트 Access Token 체크
            if (redisBlackAccessTokenService.getBlackAccessToken(token).isPresent) {
                throw JwtAuthenticationException(MsgKOR.INVALID_JWT_TOKEN.message)
            }

            if (tokenProvider.validateToken(token)) {
                val authentication = tokenProvider.getAuthentication(token)
                SecurityContextHolder.getContext().authentication = authentication
            } else {
                throw JwtAuthenticationException(MsgKOR.INVALID_JWT_TOKEN.message)
            }
        } else {
            throw JwtAuthenticationException(MsgKOR.MISSING_JWT_TOKEN.message)
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (!bearerToken.isNullOrBlank() && bearerToken.startsWith("Bearer")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}
