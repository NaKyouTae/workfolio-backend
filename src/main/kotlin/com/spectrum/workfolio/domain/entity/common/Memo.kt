package com.spectrum.workfolio.domain.entity.common

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.enums.MemoTargetType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "memos",
    indexes = [
        Index(name = "idx_memos_target_id", columnList = "taget_id"),
    ],
)
class Memo(
    content: String,
    targetType: MemoTargetType,
    targetId: String,
    isVisible: Boolean = true,
    priority: Int = 0,
) : BaseEntity("ME") {
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    var content: String = content
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", length = 64, nullable = false)
    var targetType: MemoTargetType = targetType
        protected set

    @Column(name = "target_id", length = 16, nullable = false)
    var targetId: String = targetId
        protected set

    @Column(name = "is_visible", nullable = false)
    var isVisible: Boolean = isVisible
        protected set

    @Column(name = "priority", nullable = false)
    var priority: Int = priority
        protected set

    fun changeInfo(
        content: String,
        isVisible: Boolean,
        priority: Int,
    ) {
        this.content = content
        this.isVisible = isVisible
        this.priority = priority
    }
}
