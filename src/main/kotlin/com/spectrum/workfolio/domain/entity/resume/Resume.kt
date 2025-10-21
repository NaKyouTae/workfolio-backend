package com.spectrum.workfolio.domain.entity.resume

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.enums.Gender
import com.spectrum.workfolio.utils.StringUtil
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
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
    title: String,
    description: String,
    phone: String,
    email: String,
    birthDate: LocalDate,
    gender: Gender,
    isPublic: Boolean,
    isDefault: Boolean,
    publicId: String,
    worker: Worker,
) : BaseEntity("RS") {
    @Column(name = "title", length = 1024, nullable = false)
    var title: String = title
        protected set

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    var description: String = description
        protected set

    @Column(name = "phone", length = 32, nullable = false)
    var phone: String = phone
        protected set

    @Column(name = "email", length = 516, nullable = false)
    var email: String = email
        protected set

    @Column(name = "birth_date", nullable = false)
    var birthDate: LocalDate = birthDate
        protected set

    @Column(name = "gender", nullable = false)
    var gender: Gender = gender
        protected set

    @Column(name = "is_public", nullable = false)
    var isPublic: Boolean = isPublic
        protected set

    @Column(name = "public_id", length = 16, nullable = false)
    var publicId: String = publicId
        protected set

    @Column(name = "is_default", nullable = false)
    var isDefault: Boolean = isDefault
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "worker_id", nullable = false)
    var worker: Worker = worker
        protected set

    @OneToMany(mappedBy = "resume", cascade = [CascadeType.REMOVE])
    private var mutableCareers: MutableList<Career> = mutableListOf()
    val careers: List<Career> get() = mutableCareers.toList()

    @OneToMany(mappedBy = "resume", cascade = [CascadeType.REMOVE])
    private var mutableCertifications: MutableList<Certifications> = mutableListOf()
    val certifications: List<Certifications> get() = mutableCertifications.toList()

    @OneToMany(mappedBy = "resume", cascade = [CascadeType.REMOVE])
    private var mutableDegrees: MutableList<Degrees> = mutableListOf()
    val degrees: List<Degrees> get() = mutableDegrees.toList()

    @OneToMany(mappedBy = "resume", cascade = [CascadeType.REMOVE])
    private var mutableEducations: MutableList<Education> = mutableListOf()
    val educations: List<Education> get() = mutableEducations.toList()

    @OneToMany(mappedBy = "resume", cascade = [CascadeType.REMOVE])
    private var mutableLinks: MutableList<Link> = mutableListOf()
    val links: List<Link> get() = mutableLinks.toList()

    fun changeInfo(
        title: String,
        description: String,
        phone: String,
        email: String,
        birthDate: LocalDate,
        gender: Gender,
        isPublic: Boolean,
        isDefault: Boolean,
    ) {
        this.title = title
        this.description = description
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
