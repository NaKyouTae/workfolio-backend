package com.spectrum.workfolio.utils

/**
 * 엔티티 타입 검증 유틸리티
 * AttachmentService 구현체에서 entity 파라미터 검증 시 사용
 */
object EntityTypeValidator {

    /**
     * entity가 예상한 타입인지 검증하고 반환
     *
     * @param entity 검증할 엔티티
     * @param expectedType 예상하는 타입의 클래스
     * @param serviceName 호출한 서비스 이름 (에러 메시지용)
     * @return 검증된 엔티티
     * @throws IllegalArgumentException entity가 예상 타입이 아닌 경우
     */
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> requireEntityType(
        entity: Any
    ): T {
        return if (entity is T) {
            entity
        } else {
            throw IllegalArgumentException(
                "Expected ${T::class.qualifiedName} entity but got: ${entity::class.qualifiedName}. " +
                    "requires a ${T::class.simpleName} entity."
            )
        }
    }

    /**
     * entity가 예상한 타입인지 검증 (nullable)
     * 타입이 맞지 않으면 null 반환
     */
    inline fun <reified T> requireEntityTypeOrNull(entity: Any): T? {
        return entity as? T
    }
}

