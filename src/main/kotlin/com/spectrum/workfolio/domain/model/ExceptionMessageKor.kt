package com.spectrum.workfolio.domain.model

enum class ExceptionMessageKor(val message: String) {
    USER_NOT_FOUND("존재하지 않는 사용자입니다."),
    USER_NOT_ALLOWED("회원 가입 허용된 사용자가 아닙니다."),
    USER_ID_ALREADY_EXISTS("이미 존재하는 아이디입니다."),
    REG_NO_ALREADY_EXISTS("이미 등록된 주민 등록 번호입니다."),
    INVALID_JWT_TOKEN("유효하지 않는 JWT 토큰입니다."),
    EXPIRED_JWT_TOKEN("만료된 JWT 토큰입니다."),
    UNSUPPORTED_JWT_TOKEN("지원하지 않는 JWT 토큰입니다."),
    MISSING_JWT_CLAIMS_TOKEN("정보가 부족한 JWT 토큰입니다."),
    MISSING_JWT_TOKEN("존재하지 않는 JWT 토큰입니다."),
    INCOME_NOT_FOUND("존재하지 않는 소득 정보입니다."),
    CALCULATED_TAX_NOT_FOUND("과세표준 범위가 존재하지 않습니다."),
    REFRESH_TOKEN_NOT_EXISTS("Refresh 토큰이 존재하지 않습니다."),
    INVALID_REFRESH_TOKEN_NOT_EXISTS("유효하지 않은 Refresh 토큰입니다.");
}

