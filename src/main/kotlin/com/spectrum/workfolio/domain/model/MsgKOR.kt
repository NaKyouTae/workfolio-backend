package com.spectrum.workfolio.domain.model

enum class MsgKOR(val message: String) {
    NOT_EXIST_EMAIL("존재하지 않는 이메일입니다."),
    DUPLICATION_EMAIL("중복 된 이메일입니다."),
    NOT_FOUND_WORKER("존재하지 않는 유저입니다."),
    NOT_FOUND_RECORD_GROUP("존재하지 않는 레코드 그룹입니다."),
    ALREADY_EXIST_WORKER_NICK_NAME("현재 닉네임과 동일합니다."),
    ALREADY_EXIST_WORKER_RECORD_GROUP("레코드 그룹에 이미 존재하는 멤버입니다."),
    NOT_MATCH_RECORD_GROUP_OWNER("레코드 그룹의 소유자가 아닙니다."),
    NOT_MATCH_RECORD_GROUP_EDITOR("레코드 그룹의 에디터가 아닙니다."),
    INVALID_JWT_TOKEN("유효하지 않은 토큰입니다."),
    EXPIRED_JWT_TOKEN("만료된 JWT 토큰입니다."),
    UNSUPPORTED_JWT_TOKEN("지원하지 않는 JWT 토큰입니다."),
    MISSING_JWT_CLAIMS_TOKEN("정보가 부족한 JWT 토큰입니다."),
    MISSING_JWT_TOKEN("존재하지 않는 JWT 토큰입니다."),
    NOT_EXISTS_REFRESH_TOKEN("Refresh 토큰이 존재하지 않습니다."),
    INVALID_REFRESH_TOKEN_NOT_EXISTS("유효하지 않은 Refresh 토큰입니다."),
    INSUFFICIENT_PERMISSION("권한이 부족합니다."),
}
