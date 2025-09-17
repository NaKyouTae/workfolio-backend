package com.spectrum.workfolio.config.handler

import com.spectrum.workfolio.proto.error.ErrorResponse
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.time.LocalDateTime

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(WorkfolioException::class)
    fun handleCustomException(ex: WorkfolioException): ResponseEntity<ErrorResponse> {
        return createErrorResponse(ex.message, "BAD_REQUEST", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return createErrorResponse(ex.message, "INVALID_ARGUMENT", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(BadCredentialsException::class)
    fun handleUsernameNotFoundException(ex: BadCredentialsException): ResponseEntity<ErrorResponse> {
        return createErrorResponse(ex.message, "BAD_CREDENTIALS", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleMediaTypeNotSupported(ex: HttpMediaTypeNotSupportedException): ResponseEntity<ErrorResponse> {
        return createErrorResponse(ex.message, "UNSUPPORTED_MEDIA_TYPE", HttpStatus.BAD_REQUEST)
    }

    private fun createErrorResponse(message: String?, type: String, status: HttpStatus): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse.newBuilder()
            .setTimestamp(TimeUtil.nowToLong())
            .setStatus(status.name)
            .setCode(status.value())
            .setMessage(message ?: "")
            .setPath("GLOBAL ERROR")
            .build()

        return ResponseEntity(errorResponse, status)
    }
}