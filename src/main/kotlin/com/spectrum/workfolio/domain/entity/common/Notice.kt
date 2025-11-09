package com.spectrum.workfolio.domain.entity.common

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "notices",
    indexes = [
        Index(name = "idx_notices_is_pinned_created_at", columnList = "is_pinned, created_at"),
    ],
)
class Notice(
    title: String,
    content: String,
    isPinned: Boolean = false,
) : BaseEntity("NO") {
    @Column(name = "title", length = 512, nullable = false)
    var title: String = title
        protected set

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    var content: String = content
        protected set

    @Column(name = "is_pinned", nullable = false)
    var isPinned: Boolean = isPinned
        protected set

    fun changeInfo(
        title: String,
        content: String,
        isPinned: Boolean,
    ) {
        this.title = title
        this.content = content
        this.isPinned = isPinned
    }
}

