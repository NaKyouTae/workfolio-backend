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
    name: String, // 회사명
    salary: Int, // 최종 연봉
    job: String, // 직무
    position: String, // 직책
    jobGrade: String, // 직급
    department: String, // 부서
    description: String,
    isVisible: Boolean,
    isWorking: Boolean? = null, // 재직중
    endedAt: LocalDate? = null, // 퇴사년원
    startedAt: LocalDate? = null, // 입사년월
    employmentType: EmploymentType? = null, // 재직 형태
    resume: Resume,
) : BaseEntity("CA") {
    @Column(name = "name", length = 1024, nullable = false)
    var name: String = name
        protected set

    @Column(name = "position", length = 512, nullable = false)
    var position: String = position
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 32, nullable = true)
    var employmentType: EmploymentType? = employmentType
        protected set

    @Column(name = "department", length = 512, nullable = false)
    var department: String = department
        protected set

    @Column(name = "salary", nullable = false)
    var salary: Int = salary
        protected set

    @Column(name = "job_grade", length = 512, nullable = false)
    var jobGrade: String = jobGrade
        protected set

    @Column(name = "job", length = 512, nullable = false)
    var job: String = job
        protected set

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    var description: String = description
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

    @Column(name = "is_visible", nullable = false)
    var isVisible: Boolean = isVisible
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    var resume: Resume = resume
        protected set

    @OneToMany(mappedBy = "career", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    private var mutableSalaries: MutableList<Salary> = mutableListOf()
    val salaries: List<Salary> get() = mutableSalaries.toList()

    fun changeInfo(
        name: String, // 회사명
        salary: Int, // 최종 연봉
        job: String, // 직무
        position: String, // 직책
        jobGrade: String, // 직급
        department: String, // 부서
        isVisible: Boolean,
        description: String,
        isWorking: Boolean? = null, // 재직중
        endedAt: LocalDate? = null, // 퇴사년원
        startedAt: LocalDate? = null, // 입사년월
        employmentType: EmploymentType? = null, // 재직 형태
    ) {
        this.name = name
        this.position = position
        this.employmentType = employmentType
        this.department = department
        this.salary = salary
        this.jobGrade = jobGrade
        this.job = job
        this.description = description
        this.startedAt = startedAt
        this.endedAt = endedAt
        this.isWorking = isWorking
        this.isVisible = isVisible
    }

    fun addSalary(salary: Salary) {
        mutableSalaries.add(salary)
    }

    fun removeSalary(salary: Salary) {
        mutableSalaries.remove(salary)
    }

    fun removeSalaries(salaries: List<Salary>) {
        mutableSalaries.removeAll(salaries.toSet())
    }
}
