package com.spectrum.workfolio.domain.entity.resume

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.enums.EmploymentType
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
 * 회사
 */
@Entity
@Table(
    name = "careers",
    indexes = [
        Index(name = "idx_careers_resume_id", columnList = "resume_id"),
    ],
)
class Career(
    name: String? = null, // 회사명
    salary: Int? = null, // 최종 연봉
    startedAt: LocalDate? = null, // 입사년월
    endedAt: LocalDate? = null, // 퇴사년원
    employmentType: EmploymentType? = null, // 재직 형태
    position: String? = null, // 직책
    jobGrade: String? = null, // 직급
    department: String? = null, // 부서
    job: String? = null, // 직무
    isWorking: Boolean? = null, // 재직중
    isVisible: Boolean? = null,
    resume: Resume,
) : BaseEntity("CA") {
    @Column(name = "name", length = 1024, nullable = true)
    var name: String? = name
        protected set

    @Column(name = "position", length = 512, nullable = true)
    var position: String? = position
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 32, nullable = true)
    var employmentType: EmploymentType? = employmentType
        protected set

    @Column(name = "department", length = 512, nullable = true)
    var department: String? = department
        protected set

    @Column(name = "salary", nullable = false)
    var salary: Int? = salary
        protected set

    @Column(name = "job_grade", length = 512, nullable = true)
    var jobGrade: String? = jobGrade
        protected set

    @Column(name = "job", length = 512, nullable = true)
    var job: String? = job
        protected set

    @Column(name = "started_at", nullable = true)
    var startedAt: LocalDate? = startedAt
        protected set

    @Column(name = "ended_at", nullable = true)
    var endedAt: LocalDate? = endedAt
        protected set

    @Column(name = "is_working", nullable = true)
    var isWorking: Boolean? = isWorking
        protected set

    @Column(name = "is_visible", nullable = true)
    var isVisible: Boolean? = isVisible
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    var resume: Resume = resume
        protected set

    @OneToMany(mappedBy = "career", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    private var mutableSalaries: MutableList<Salary> = mutableListOf()
    val salaries: List<Salary> get() = mutableSalaries.toList()

    @OneToMany(mappedBy = "career", cascade = [CascadeType.REMOVE], fetch = FetchType.LAZY)
    private var mutableAchievements: MutableList<Achievement> = mutableListOf()
    val achievements: List<Achievement> get() = mutableAchievements.toList()

    fun changeInfo(
        name: String? = null,
        position: String? = null,
        employmentType: EmploymentType? = null,
        department: String? = null,
        salary: Int? = null,
        jobGrade: String? = null,
        job: String? = null,
        startedAt: LocalDate? = null,
        endedAt: LocalDate?? = null,
        isWorking: Boolean? = null,
        isVisible: Boolean? = null,
    ) {
        this.name = name
        this.position = position
        this.employmentType = employmentType
        this.department = department
        this.salary = salary
        this.jobGrade = jobGrade
        this.job = job
        this.startedAt = startedAt
        this.endedAt = endedAt
        this.isWorking = isWorking
        this.isVisible = isVisible
    }

    fun addAchievement(achievement: Achievement) {
        mutableAchievements.add(achievement)
    }

    fun addSalary(salary: Salary) {
        mutableSalaries.add(salary)
    }
}
