package com.spectrum.workfolio.domain.entity.resume

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.enums.Language
import com.spectrum.workfolio.domain.enums.LanguageLevel
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

/**
 * 언어 능력
 */
@Entity
@Table(
    name = "language_skills",
    indexes = [
        Index(name = "idx_language_skills_resume_id_priority", columnList = "resume_id, priority"),
    ],
)
class LanguageSkill(
    language: Language? = null,
    level: LanguageLevel? = null,
    isVisible: Boolean,
    priority: Int = 0,
    resume: Resume,
) : BaseEntity("LS") {

    @Enumerated(EnumType.STRING)
    @Column(name = "language", length = 64, nullable = true)
    var language: Language? = language
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "level", length = 256, nullable = true)
    var level: LanguageLevel? = level
        protected set

    @Column(name = "is_visible", nullable = false)
    var isVisible: Boolean = isVisible
        protected set

    @Column(name = "priority", nullable = false)
    var priority: Int = priority
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    var resume: Resume = resume
        protected set

    @OneToMany(mappedBy = "languageSkill", cascade = [jakarta.persistence.CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableLanguageTests: MutableList<LanguageTest> = mutableListOf()
    val languageTests: List<LanguageTest> get() = mutableLanguageTests.toList()

    fun changeInfo(
        language: Language? = null,
        level: LanguageLevel? = null,
        isVisible: Boolean,
        priority: Int = 0,
    ) {
        this.language = language
        this.level = level
        this.isVisible = isVisible
        this.priority = priority
    }
}
