package com.spectrum.workfolio.domain.enums

enum class CreditTxType {
    CHARGE,      // 충전 (결제)
    BONUS,       // 보너스 지급
    USE,         // 사용
    REFUND,      // 환불 차감
    ADMIN_ADD,   // 관리자 지급
    ADMIN_DEDUCT // 관리자 차감
}
