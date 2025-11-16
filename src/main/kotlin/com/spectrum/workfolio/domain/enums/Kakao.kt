package com.spectrum.workfolio.domain.enums

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    val profile: Profile, // 프로필 정보
    val name: String, // 카카오계정 이름
    @JsonProperty("phone_number")
    val phoneNumber: String? = null, // 전화번호
    val email: String? = null, // 카카오계정 이메일
    val birthyear: String? = null, // 출생 연도
    val birthday: String? = null, // 생일 (MMDD 형식)
    val gender: String? = null, // 성별 (female, male)
    @JsonProperty("age_range")
    val ageRange: String? = null, // 연령대
    val ci: String? = null, // 연계정보 (CI)

    @JsonProperty("profile_needs_agreement")
    val profileNeedsAgreement: Boolean? = null, // 사용자 동의 시 프로필 정보 제공 가능 여부
    @JsonProperty("profile_nickname_needs_agreement")
    val profileNicknameNeedsAgreement: Boolean? = null, // 닉네임 동의 여부
    @JsonProperty("profile_image_needs_agreement")
    val profileImageNeedsAgreement: Boolean? = null, // 프로필 사진 동의 여부
    @JsonProperty("name_needs_agreement")
    val nameNeedsAgreement: Boolean? = null, // 이름 동의 여부
    @JsonProperty("has_email")
    val hasEmail: Boolean, // 이메일 보유 여부
    @JsonProperty("email_needs_agreement")
    val emailNeedsAgreement: Boolean? = null, // 이메일 동의 여부
    @JsonProperty("is_email_valid")
    val isEmailValid: Boolean? = null, // 이메일 유효 여부
    @JsonProperty("is_email_verified")
    val isEmailVerified: Boolean? = null, // 이메일 인증 여부
    @JsonProperty("has_phone_number")
    val hasPhoneNumber: Boolean, // 전화번호 보유 여부
    @JsonProperty("phone_number_needs_agreement")
    val phoneNumberNeedsAgreement: Boolean? = null, // 전화번호 동의 여부
    @JsonProperty("has_birthyear")
    val hasBirthyear: Boolean, // 출생 연도 보유 여부
    @JsonProperty("birthyear_needs_agreement")
    val birthyearNeedsAgreement: Boolean? = null, // 출생 연도 동의 여부
    @JsonProperty("has_birthday")
    val hasBirthday: Boolean, // 생일 보유 여부
    @JsonProperty("birthday_needs_agreement")
    val birthdayNeedsAgreement: Boolean? = null, // 생일 동의 여부
    @JsonProperty("birthday_type")
    val birthdayType: String? = null, // 생일 타입 (SOLAR: 양력, LUNAR: 음력)
    @JsonProperty("is_leap_month")
    val isLeapMonth: Boolean? = null, // 윤달 여부
    @JsonProperty("has_gender")
    val hasGender: Boolean, // 성별 보유 여부
    @JsonProperty("gender_needs_agreement")
    val genderNeedsAgreement: Boolean? = null, // 성별 동의 여부
    @JsonProperty("age_range_needs_agreement")
    val ageRangeNeedsAgreement: Boolean? = null, // 연령대 동의 여부
    @JsonProperty("ci_needs_agreement")
    val ciNeedsAgreement: Boolean? = null, // CI 동의 여부
    @JsonProperty("ci_authenticated_at")
    val ciAuthenticatedAt: String? = null, // CI 발급 시각, UTC
) {
    /**
     * birthyear와 birthday를 결합하여 LocalDate로 변환
     * @return birthyear와 birthday가 모두 존재하면 LocalDate, 아니면 null
     */
    fun getBirthDate(): LocalDate? {
        if (birthyear == null || birthday == null) {
            return null
        }

        val dateString = birthyear + birthday // "19930804" 형식
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"))
    }

    fun getGender(): Gender? {
        if(this.gender == null) {
            return null
        }
        return Gender.valueOf(gender.uppercase())
    }

    /**
     * 전화번호를 일반적인 형식으로 변환 ("+82 10-9109-2682" -> "01091092682")
     * @return 숫자만 포함된 전화번호 문자열
     */
    fun getNormalizedPhoneNumber(): String {
        if(phoneNumber == null) {
            return ""
        }

        return phoneNumber
            .replace("+82 ", "0") // 국가 코드 +82를 0으로 변환
            .replace(Regex("[\\s-]"), "") // 공백과 하이픈 제거
    }
}

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
