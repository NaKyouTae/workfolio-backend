package com.spectrum.workfolio.config.filter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.spectrum.workfolio.config.exception.JwtAuthenticationException
import com.spectrum.workfolio.proto.error.ErrorResponse
import com.spectrum.workfolio.utils.TimeUtil
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtExceptionFilter : OncePerRequestFilter() {

    @Throws(ServletException::class, java.io.IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            filterChain.doFilter(request, response)
        } catch (ex: JwtAuthenticationException) {
            setResponse(request.requestURI, response, HttpStatus.UNAUTHORIZED, ex)
        } catch (ex: java.io.IOException) {
            setResponse(request.requestURI, response, HttpStatus.INTERNAL_SERVER_ERROR, ex)
        }
    }

    @Throws(RuntimeException::class, java.io.IOException::class)
    private fun setResponse(
        path: String,
        response: HttpServletResponse,
        status: HttpStatus,
        ex: Throwable,
    ) {
//        val errorResponse = ErrorResponse.newBuilder().setTimestamp(TimeUtil.nowToLong()).setStatus(status.name)
//            .setCode(status.value()).setMessage(ex.message).setPath(path).build()
        val objectMapper = jacksonObjectMapper()
        val jsonResponse = objectMapper.writeValueAsString(ex.message)

        response.status = status.value()
        response.contentType = "application/json;charset=UTF-8"
        response.writer.print(jsonResponse)
    }
}