package com.spectrum.workfolio.domain.model

import com.fasterxml.jackson.annotation.JsonProperty

data class KakaoTokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("token_type")
    val tokenType: String,
    @JsonProperty("refresh_token")
    val refreshToken: String,
    @JsonProperty("expires_in")
    val expiresIn: Int,
    @JsonProperty("refresh_token_expires_in")
    val refreshTokenExpiresIn: Int,
)

data class KakaoDTO(
    val id: Long, // 회원번호
    @JsonProperty("has_signed_up")
    val hasSignedUp: Boolean?, // 자동 연결 설정을 비활성화한 경우만 존재
    @JsonProperty("connected_at")
    val connectedAt: String?, // 서비스에 연결 완료된 시각, UTC
    @JsonProperty("synched_at")
    val synchedAt: String?, // 카카오싱크 간편가입으로 로그인한 시각, UTC
    val properties: Map<String, Any>?, // 사용자 프로퍼티
    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount, // 카카오계정 정보
    @JsonProperty("for_partner")
    val forPartner: Partner?, // uuid 등 추가 정보
)

data class KakaoAccount(
    @JsonProperty("profile_needs_agreement")
    val profileNeedsAgreement: Boolean, // 사용자 동의 시 프로필 정보 제공 가능 여부
    @JsonProperty("profile_nickname_needs_agreement")
    val profileNicknameNeedsAgreement: Boolean, // 닉네임 동의 여부
    @JsonProperty("profile_image_needs_agreement")
    val profileImageNeedsAgreement: Boolean, // 프로필 사진 동의 여부
    val profile: Profile, // 프로필 정보
    @JsonProperty("name_needs_agreement")
    val nameNeedsAgreement: Boolean, // 이름 동의 여부
    val name: String?, // 카카오계정 이름
    @JsonProperty("email_needs_agreement")
    val emailNeedsAgreement: Boolean, // 이메일 동의 여부
    @JsonProperty("is_email_valid")
    val isEmailValid: Boolean, // 이메일 유효 여부
    @JsonProperty("is_email_verified")
    val isEmailVerified: Boolean, // 이메일 인증 여부
    val email: String?, // 카카오계정 이메일
    @JsonProperty("age_range_needs_agreement")
    val ageRangeNeedsAgreement: Boolean, // 연령대 동의 여부
    val ageRange: String?, // 연령대
    @JsonProperty("birthyear_needs_agreement")
    val birthyearNeedsAgreement: Boolean, // 출생 연도 동의 여부
    val birthyear: String?, // 출생 연도
    @JsonProperty("birthday_needs_agreement")
    val birthdayNeedsAgreement: Boolean, // 생일 동의 여부
    val birthday: String?, // 생일 (MMDD 형식)
    val birthdayType: String?, // 생일 타입 (양력, 음력)
    @JsonProperty("is_leap_month")
    val isLeapMonth: Boolean?, // 윤달 여부
    @JsonProperty("gender_needs_agreement")
    val genderNeedsAgreement: Boolean, // 성별 동의 여부
    val gender: String?, // 성별 (female, male)
    @JsonProperty("phone_number_needs_agreement")
    val phoneNumberNeedsAgreement: Boolean, // 전화번호 동의 여부
    val phoneNumber: String?, // 전화번호
    @JsonProperty("ci_needs_agreement")
    val ciNeedsAgreement: Boolean, // CI 동의 여부
    val ci: String?, // 연계정보 (CI)
    @JsonProperty("ci_authenticated_at")
    val ciAuthenticatedAt: String?, // CI 발급 시각, UTC
)

data class Profile(
    val nickname: String, // 닉네임
    @JsonProperty("thumbnail_image_url")
    val thumbnailImageUrl: String?, // 프로필 미리보기 이미지 URL
    @JsonProperty("profile_image_url")
    val profileImageUrl: String?, // 프로필 사진 URL
    @JsonProperty("is_default_image")
    val isDefaultImage: Boolean, // 기본 프로필 사진 여부
    @JsonProperty("is_default_nickname")
    val isDefaultNickname: Boolean, // 기본 닉네임 여부
)

data class Partner(
    val uuid: String?, // 고유 ID (카카오톡 메시지 API 사용 권한이 있는 경우만 제공)
)
