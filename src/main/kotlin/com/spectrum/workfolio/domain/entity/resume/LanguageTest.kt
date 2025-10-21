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
        Index(name = "idx_language_tests_language_skill_id", columnList = "language_skill_id"),
    ],
)
class LanguageTest(
    name: String? = null,
    score: String? = null,
    acquiredAt: LocalDate? = null,
    isVisible: Boolean? = null,
    languageSkill: LanguageSkill,
) : BaseEntity("LT") {
    
    @Column(name = "name", length = 1024, nullable = true)
    var name: String? = name
        protected set
    
    @Column(name = "score", length = 256, nullable = true)
    var score: String? = score
        protected set
    
    @Column(name = "acquired_at", nullable = true)
    var acquiredAt: LocalDate? = acquiredAt
        protected set
    
    @Column(name = "is_visible", nullable = true)
    var isVisible: Boolean? = isVisible
        protected set
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_skill_id", nullable = false)
    var languageSkill: LanguageSkill = languageSkill
        protected set
    
    fun changeInfo(
        name: String? = null,
        score: String? = null,
        acquiredAt: LocalDate? = null,
        isVisible: Boolean? = null,
    ) {
        this.name = name
        this.score = score
        this.acquiredAt = acquiredAt
        this.isVisible = isVisible
    }
}
