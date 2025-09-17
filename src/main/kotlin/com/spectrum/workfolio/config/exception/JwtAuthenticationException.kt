package com.spectrum.workfolio.config.exception

import org.springframework.security.core.AuthenticationException

class JwtAuthenticationException(
    message: String,
) : AuthenticationException(message)