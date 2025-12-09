package com.spectrum.workfolio.config.repository

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * OAuth2AuthorizationRequest를 JSON으로 직렬화/역직렬화하기 위한 DTO
 * OAuth2AuthorizationRequest는 Jackson으로 직접 역직렬화가 어려우므로 DTO를 사용
 */
data class OAuth2AuthorizationRequestDTO(
    @JsonProperty("authorizationUri") val authorizationUri: String,
    @JsonProperty("clientId") val clientId: String,
    @JsonProperty("redirectUri") val redirectUri: String,
    @JsonProperty("scopes") val scopes: Set<String> = emptySet(),
    @JsonProperty("state") val state: String,
    @JsonProperty("additionalParameters") val additionalParameters: Map<String, String> = emptyMap(),
    @JsonProperty("authorizationRequestUri") val authorizationRequestUri: String? = null,
    @JsonProperty("attributes") val attributes: Map<String, Any> = emptyMap(),
)
