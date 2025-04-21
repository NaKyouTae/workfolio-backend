package com.spectrum.workfolio.config

import com.spectrum.workfolio.config.entry.WorkfolioAuthenticationEntryPoint
import com.spectrum.workfolio.config.filter.JwtAuthFilter
import com.spectrum.workfolio.config.handler.WorkfolioAccessDeniedHandler
import com.spectrum.workfolio.config.handler.WorkfolioOAuth2LogoutSuccessHandler
import com.spectrum.workfolio.config.handler.WorkfolioOAuth2LoginFailureHandler
import com.spectrum.workfolio.config.handler.WorkfolioOAuth2LoginSuccessHandler
import com.spectrum.workfolio.config.provider.JwtTokenProvider
import com.spectrum.workfolio.config.repository.OAuth2AuthorizationRequestBasedOnCookieRepository
import com.spectrum.workfolio.config.resolver.CustomAuthorizationRequestResolver
import com.spectrum.workfolio.config.service.WorkerDetailService
import com.spectrum.workfolio.config.service.WorkfolioOAuth2UserService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
class SecurityConfig(
    private val jwtTokenProvider: JwtTokenProvider,
    private val workerDetailsService: WorkerDetailService,
    private val clientRegistrationRepository: ClientRegistrationRepository,
    private val workfolioAuthenticationEntryPoint: WorkfolioAuthenticationEntryPoint,
    private val workfolioAccessDeniedHandler: WorkfolioAccessDeniedHandler,
    private val workfolioOAuth2UserService: WorkfolioOAuth2UserService,
    private val workfolioOAuth2LoginFailureHandler: WorkfolioOAuth2LoginFailureHandler,
    private val workfolioOAuth2LoginSuccessHandler: WorkfolioOAuth2LoginSuccessHandler,
    private val workfolioOAuth2LogoutSuccessHandler: WorkfolioOAuth2LogoutSuccessHandler,
    private val oAuth2AuthorizationRequestBasedOnCookieRepository: OAuth2AuthorizationRequestBasedOnCookieRepository,
) {

    @Bean
    fun filterChain(http: HttpSecurity) = http
        .csrf { it.disable() } // csrf 비활성화 -> cookie를 사용하지 않으면 꺼도 된다. (cookie를 사용할 경우 httpOnly(XSS 방어), sameSite(CSRF 방어)로 방어해야 한다.)
        .httpBasic { it.disable() } // csrf 비활성화 -> cookie를 사용하지 않으면 꺼도 된다. (cookie를 사용할 경우 httpOnly(XSS 방어), sameSite(CSRF 방어)로 방어해야 한다.)
        .formLogin { it.disable() } // csrf 비활성화 -> cookie를 사용하지 않으면 꺼도 된다. (cookie를 사용할 경우 httpOnly(XSS 방어), sameSite(CSRF 방어)로 방어해야 한다.)
        .headers { it.frameOptions { frameOptions -> frameOptions.disable() } }
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
        .authorizeHttpRequests {
            it
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/login/**", "/error", "/favicon.ico").permitAll()
                .anyRequest().authenticated()
        }
        .oauth2Login { oauth2 ->
            oauth2
                .failureHandler(workfolioOAuth2LoginFailureHandler)
                .successHandler(workfolioOAuth2LoginSuccessHandler)
                .userInfoEndpoint { userInfoEndpoint ->
                    userInfoEndpoint.userService(workfolioOAuth2UserService)
                }
                .authorizationEndpoint {
                    it
                        .baseUri("/oauth2/authorization")
                        .authorizationRequestRepository(oAuth2AuthorizationRequestBasedOnCookieRepository)
                        .authorizationRequestResolver(
                            CustomAuthorizationRequestResolver(
                                clientRegistrationRepository,
                                "/oauth2/authorization"
                            )
                        )
                }
        }
        .logout {
            it
                .logoutUrl("/api/logout")
                .logoutSuccessHandler(workfolioOAuth2LogoutSuccessHandler) // 커스텀 핸들러 적용
                .invalidateHttpSession(true) // 세션 무효화
        }
        .exceptionHandling {
            it.authenticationEntryPoint(workfolioAuthenticationEntryPoint) // 인증 실패 시 401 응답
            it.accessDeniedHandler(workfolioAccessDeniedHandler) // 권한 부족 시 401 응답
        }
        .cors { it.configurationSource(corsConfigurationSource()) }
        .addFilterBefore(
            JwtAuthFilter(jwtTokenProvider, workerDetailsService),
            BasicAuthenticationFilter::class.java
        )
        .build()!!

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        configuration.allowedOrigins = listOf("http://localhost:3000", "http://127.0.0.1:3000")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
