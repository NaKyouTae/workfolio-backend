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
        Index(name = "idx_careers_name", columnList = "name"),
        Index(name = "idx_careers_resume_id", columnList = "resume_id"),
    ],
)
class Career(
    name: String, // 회사명
    position: String, // 직책
    employmentType: EmploymentType, // 재직 형태
    department: String, // 부서
    salary: Int, // 연봉
    jobGrade: String, // 직급
    job: String, // 직무
    startedAt: LocalDate, // 입사년월
    endedAt: LocalDate? = null, // 퇴사년원
    isWorking: Boolean = false, // 재직중
    isVisible: Boolean = false,
    resume: Resume,
) : BaseEntity("CA") {
    @Column(name = "name", length = 512, nullable = false, unique = true)
    var name: String = name
        protected set

    @Column(name = "is_visible", nullable = false)
    var isVisible: Boolean = isVisible
        protected set

    @Column(name = "position", length = 256, nullable = false)
    var position: String = position

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 32, nullable = false)
    var employmentType: EmploymentType = employmentType

    @Column(name = "department", length = 256, nullable = false)
    var department: String = department

    @Column(name = "salary", nullable = false)
    var salary: Int = salary

    @Column(name = "job_grade", length = 128, nullable = false)
    var jobGrade: String = jobGrade

    @Column(name = "job", length = 128, nullable = false)
    var job: String = job

    @Column(name = "started_at", nullable = false)
    var startedAt: LocalDate = startedAt
        protected set

    @Column(name = "ended_at", nullable = true)
    var endedAt: LocalDate? = endedAt
        protected set

    @Column(name = "is_working", nullable = false)
    var isWorking: Boolean = isWorking
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    var resume: Resume = resume
        protected set

    @OneToMany(mappedBy = "career", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    private var mutableProjects: MutableList<Project> = mutableListOf()
    val projects: List<Project> get() = mutableProjects.toList()

    @OneToMany(mappedBy = "career", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    private var mutableSalaries: MutableList<Salary> = mutableListOf()
    val salaries: List<Salary> get() = mutableSalaries.toList()

    fun changeInfo(
        name: String,
        position: String,
        employmentType: EmploymentType,
        department: String,
        salary: Int,
        jobGrade: String,
        job: String,
        startedAt: LocalDate,
        endedAt: LocalDate?,
        isWorking: Boolean,
        isVisible: Boolean,
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

    fun addProject(project: Project) {
        mutableProjects.add(project)
    }

    fun addSalary(salary: Salary) {
        mutableSalaries.add(salary)
    }
}
