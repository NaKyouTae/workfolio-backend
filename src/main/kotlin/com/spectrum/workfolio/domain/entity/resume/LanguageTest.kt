package com.spectrum.workfolio.domain.entity.resume

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

/**
 * 언어 시험
 */
@Entity
@Table(
    name = "language_tests",
    indexes = [
        Index(name = "idx_language_tests_language_skill_id_priority", columnList = "language_skill_id, priority"),
    ],
)
class LanguageTest(
    name: String,
    score: String,
    acquiredAt: LocalDate? = null,
    isVisible: Boolean,
    priority: Int = 0,
    languageSkill: LanguageSkill,
) : BaseEntity("LT") {

    @Column(name = "name", length = 1024, nullable = false)
    var name: String = name
        protected set

    @Column(name = "score", length = 512, nullable = false)
    var score: String = score
        protected set

    @Column(name = "acquired_at", nullable = true)
    var acquiredAt: LocalDate? = acquiredAt
        protected set

    @Column(name = "is_visible", nullable = false)
    var isVisible: Boolean = isVisible
        protected set

    @Column(name = "priority", nullable = false)
    var priority: Int = priority
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_skill_id", nullable = false)
    var languageSkill: LanguageSkill = languageSkill
        protected set

    fun changeInfo(
        name: String,
        score: String,
        acquiredAt: LocalDate? = null,
        isVisible: Boolean,
        priority: Int = 0,
    ) {
        this.name = name
        this.score = score
        this.acquiredAt = acquiredAt
        this.isVisible = isVisible
        this.priority = priority
    }
}
