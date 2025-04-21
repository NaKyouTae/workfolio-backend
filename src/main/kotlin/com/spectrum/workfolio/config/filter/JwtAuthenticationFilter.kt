package com.spectrum.workfolio.config.filter

import com.spectrum.workfolio.config.provider.JwtTokenProvider
import com.spectrum.workfolio.config.service.WorkerDetailService
import io.jsonwebtoken.io.IOException
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.security.core.userdetails.UserDetails

class JwtAuthFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val workerDetailsService: WorkerDetailService,
) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val excludePath = listOf("/login", "/logout", "/error", "/favicon.ico")
        val path = request.requestURI
        return excludePath.any { path.startsWith(it) }
    }

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val authorizationHeader = request.getHeader("Authorization")

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            val token = authorizationHeader.removePrefix("Bearer ")

            try {
                if (!jwtTokenProvider.validateToken(token)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired JWT token")
                    return
                }

                val workerId = jwtTokenProvider.getWorkerId(token)
                val userDetails: UserDetails = workerDetailsService.loadUserByUsername(workerId)

                val authentication = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                SecurityContextHolder.getContext().authentication = authentication

            } catch (e: Exception) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.message)
                return
            }
        }

        filterChain.doFilter(request, response)
    }
}

