package com.spectrum.workfolio.utils

import com.google.protobuf.ProtocolMessageEnum

/**
 * Proto Enum을 Kotlin Enum으로 안전하게 변환하는 유틸리티
 */
object EnumUtils {

    /**
     * Proto Enum이 UNKNOWN이 아닐 때만 Kotlin Enum으로 변환
     * @param protoEnum proto enum 값
     * @param converter proto enum name을 받아 Kotlin enum으로 변환하는 함수
     * @return Kotlin enum 또는 null (UNKNOWN인 경우)
     */
    inline fun <reified T : Enum<T>> convertProtoEnum(
        protoEnum: ProtocolMessageEnum,
        converter: (String) -> T,
    ): T? {
        // UNKNOWN 계열 enum 이름들
        val unknownNames = setOf("UNKNOWN", "LANGUAGE_UNKNOWN", "LANGUAGE_LEVEL_UNKNOWN")
        val enumName = protoEnum.valueDescriptor.name

        return if (enumName in unknownNames) {
            null
        } else {
            try {
                converter(enumName)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }

    /**
     * Proto Enum이 UNKNOWN이 아닐 때만 Kotlin Enum으로 변환 (valueOf 사용)
     * @param protoEnum proto enum 값
     * @return Kotlin enum 또는 null (UNKNOWN인 경우)
     */
    inline fun <reified T : Enum<T>> convertProtoEnumSafe(protoEnum: ProtocolMessageEnum): T? {
        return convertProtoEnum(protoEnum) { name ->
            enumValueOf<T>(name)
        }
    }

    /**
     * Proto Enum number가 0이 아닌지 확인
     */
    fun ProtocolMessageEnum.isNotUnknown(): Boolean {
        val unknownNames = setOf("UNKNOWN", "LANGUAGE_UNKNOWN", "LANGUAGE_LEVEL_UNKNOWN")
        val enumName = this.valueDescriptor.name
        return this.number != 0 && enumName !in unknownNames
    }
}
