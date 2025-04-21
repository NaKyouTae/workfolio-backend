package com.spectrum.workfolio.config.resolver

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.config.provider.JwtTokenProvider
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class AuthUserArgumentResolver(
    private val jwtTokenProvider: JwtTokenProvider,
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(AuthenticatedUser::class.java) &&
                parameter.parameterType == String::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val request: HttpServletRequest = webRequest.getNativeRequest(HttpServletRequest::class.java)!!
        val token = request.getHeader("Authorization")?.removePrefix("Bearer ") ?: throw IllegalArgumentException("토큰이 없습니다.")

        return jwtTokenProvider.getWorkerId(token)
    }
}
