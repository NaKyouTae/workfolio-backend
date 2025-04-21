package com.spectrum.workfolio.domain.model

enum class MsgKOR(val message: String) {
    NOT_EXIST_EMAIL("존재하지 않는 이메일입니다."),
    DUPLICATION_EMAIL("중복 된 이메일입니다."),
    USER_NOT_FOUND("존재하지 않는 유저입니다."),
    NOT_MATCH_PASSWORD("일치하지 않는 비밀번호입니다."),
    INVALID_PASSWORD_CHANGE_DATE("90일간 비밀번호를 변경하지 않아 로그인할 수 없습니다."),
    NOT_EXIST_TOKEN("Authorization 헤더가 누락 되었습니다."),
    EXPIRED_TOKEN("토큰이 만료되었습니다."),
    FORBIDDEN("권한이 부족합니다.")
}
