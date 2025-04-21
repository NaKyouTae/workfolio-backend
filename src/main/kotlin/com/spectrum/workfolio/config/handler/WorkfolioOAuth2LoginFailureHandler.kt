package com.spectrum.workfolio.config.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class WorkfolioOAuth2LoginFailureHandler : AuthenticationFailureHandler {
    override fun onAuthenticationFailure(request: HttpServletRequest, response: HttpServletResponse, exception: AuthenticationException) {
        println("Authentication failure: ${exception.message}")
        val code = request.getParameter("code")
        println("Received authorization code: $code")

        val redirectUrl = buildRedirectUrl()
        response.sendRedirect(redirectUrl)
    }

    private fun buildRedirectUrl(): String {
        return UriComponentsBuilder.fromUriString("http://localhost:3000/error").toUriString()
    }
}


