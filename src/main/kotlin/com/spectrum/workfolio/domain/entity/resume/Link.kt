package com.spectrum.workfolio.domain.entity.resume

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

/**
 * 이력서
 */
@Entity
@Table(
    name = "links",
    indexes = [
        Index(name = "idx_links_resume_id", columnList = "resume_id"),
    ],
)
class Link(
    url: String,
    isVisible: Boolean,
    resume: Resume,
) : BaseEntity("LI") {

    @Column(name = "url", columnDefinition = "TEXT", nullable = false)
    var url: String = url
        protected set

    @Column(name = "is_visible", nullable = false)
    var isVisible: Boolean = isVisible
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    var resume: Resume = resume

    fun changeInfo(
        url: String,
        isVisible: Boolean,
    ) {
        this.url = url
        this.isVisible = isVisible
    }
}
