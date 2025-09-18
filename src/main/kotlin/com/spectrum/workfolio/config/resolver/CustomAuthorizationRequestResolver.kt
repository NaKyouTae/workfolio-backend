package com.spectrum.workfolio.config.resolver

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest

class CustomAuthorizationRequestResolver(
    clientRegistrationRepository: ClientRegistrationRepository,
    private val authorizationRequestBaseUri: String,
) : OAuth2AuthorizationRequestResolver {

    private val defaultResolver =
        DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, authorizationRequestBaseUri)

    override fun resolve(request: HttpServletRequest): OAuth2AuthorizationRequest? {
        return customize(defaultResolver.resolve(request))
    }

    override fun resolve(request: HttpServletRequest, clientRegistrationId: String): OAuth2AuthorizationRequest? {
        return customize(defaultResolver.resolve(request, clientRegistrationId))
    }

    private fun customize(authorizationRequest: OAuth2AuthorizationRequest?): OAuth2AuthorizationRequest? {
        if (authorizationRequest == null) return null

        return OAuth2AuthorizationRequest.from(authorizationRequest)
            .additionalParameters { it["prompt"] = "login" }
            .build()
    }
}
