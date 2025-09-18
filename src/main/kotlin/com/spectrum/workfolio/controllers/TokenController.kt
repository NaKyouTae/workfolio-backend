package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.proto.token.TokenResponse
import com.spectrum.workfolio.services.AuthService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/token")
class TokenController(
    private val authService: AuthService,
) {

    @GetMapping("/reissue")
    fun reissue(
        @RequestHeader("Authorization") accessToken: String,
        @RequestHeader("RefreshToken") refreshToken: String,
    ): TokenResponse {
        val token = authService.reissueAccessToken(accessToken, refreshToken)
        return TokenResponse.newBuilder().setAccessToken(token.accessToken).setRefreshToken(token.refreshToken).build()
    }
}
