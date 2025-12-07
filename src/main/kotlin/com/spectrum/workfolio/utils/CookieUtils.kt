package com.spectrum.workfolio.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.*

// 예시로 CookieUtil에 대한 직렬화 및 역직렬화 구현
object CookieUtils {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    fun getCookie(request: HttpServletRequest, name: String): Optional<Cookie> {
        val cookies = request.cookies ?: return Optional.empty()
        return cookies.firstOrNull { it.name == name }?.let { Optional.of(it) } ?: Optional.empty()
    }

    fun addCookie(response: HttpServletResponse, name: String, value: String, maxAge: Int) {
        val cookie = Cookie(name, value).apply {
            path = "/"
            isHttpOnly = true
            this.maxAge = maxAge
        }
        response.addCookie(cookie)
        response.setHeader("Set-Cookie", "$name=$value; Path=/; HttpOnly; Secure; SameSite=None")
    }

    fun deleteCookie(request: HttpServletRequest, response: HttpServletResponse, name: String) {
        val cookies = request.cookies ?: return
        cookies.filter { it.name == name }.forEach {
            it.value = ""
            it.path = "/"
            it.maxAge = 0
            response.addCookie(it)
        }
    }

    fun serialize(obj: Any): String {
        val jsonBytes = objectMapper.writeValueAsBytes(obj)
        return Base64.getUrlEncoder().encodeToString(jsonBytes)
    }

    fun <T> deserialize(cookie: Cookie, cls: Class<T>): T {
        val decodedBytes = Base64.getUrlDecoder().decode(cookie.value)
        val typeFactory = TypeFactory.defaultInstance()
        val javaType = typeFactory.constructType(cls)
        return objectMapper.readValue(decodedBytes, javaType)
    }
}
