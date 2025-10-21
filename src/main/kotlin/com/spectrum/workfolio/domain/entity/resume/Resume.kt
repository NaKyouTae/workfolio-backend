package com.spectrum.workfolio.domain.entity.resume

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.enums.Gender
import com.spectrum.workfolio.utils.StringUtil
import jakarta.persistence.CascadeType
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
import java.time.LocalDate

/**
 * 이력서
 */
@Entity
@Table(
    name = "resumes",
    indexes = [
        Index(name = "idx_resumes_worker_id", columnList = "worker_id"),
    ],
)
class Resume(
    title: String? = null,
    name: String? = null,
    birthDate: LocalDate? = null,
    gender: Gender? = null,
    phone: String? = null,
    email: String? = null,
    isPublic: Boolean? = null,
    isDefault: Boolean? = null,
    publicId: String? = null,
    worker: Worker,
) : BaseEntity("RS") {
    @Column(name = "title", length = 1024, nullable = true)
    var title: String? = title
        protected set

    @Column(name = "name", length = 1024, nullable = true)
    var name: String? = name
        protected set

    @Column(name = "phone", length = 32, nullable = true)
    var phone: String? = phone
        protected set

    @Column(name = "email", length = 516, nullable = true)
    var email: String? = email
        protected set

    @Column(name = "birth_date", nullable = true)
    var birthDate: LocalDate? = birthDate
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 24, nullable = true)
    var gender: Gender? = gender
        protected set

    @Column(name = "public_id", length = 16, nullable = true)
    var publicId: String? = publicId
        protected set

    @Column(name = "is_public", nullable = true)
    var isPublic: Boolean? = isPublic
        protected set

    @Column(name = "is_default", nullable = true)
    var isDefault: Boolean? = isDefault
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set

    @OneToMany(mappedBy = "resume", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableCareers: MutableList<Career> = mutableListOf()
    val careers: List<Career> get() = mutableCareers.toList()

    @OneToMany(mappedBy = "resume", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableEducations: MutableList<Education> = mutableListOf()
    val educations: List<Education> get() = mutableEducations.toList()

    @OneToMany(mappedBy = "resume", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableActivities: MutableList<Activity> = mutableListOf()
    val activities: List<Activity> get() = mutableActivities.toList()

    @OneToMany(mappedBy = "resume", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableLanguageSkills: MutableList<LanguageSkill> = mutableListOf()
    val languageSkills: List<LanguageSkill> get() = mutableLanguageSkills.toList()

    @OneToMany(mappedBy = "resume", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableAttachments: MutableList<Attachment> = mutableListOf()
    val attachments: List<Attachment> get() = mutableAttachments.toList()

    fun changeInfo(
        title: String? = null,
        name: String? = null,
        phone: String? = null,
        email: String? = null,
        birthDate: LocalDate? = null,
        gender: Gender? = null,
        isPublic: Boolean? = null,
        isDefault: Boolean? = null,
    ) {
        this.title = title
        this.name = name
        this.phone = phone
        this.email = email
        this.birthDate = birthDate
        this.gender = gender
        this.isPublic = isPublic
        this.isDefault = isDefault
    }

    companion object {
        fun generatePublicId(): String {
            return StringUtil.generateRandomString(16)
        }
    }
}
