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
    name: String,
    isVisible: Boolean = false,
    position: String,
    employmentType: EmploymentType,
    department: String,
    startedAt: LocalDate,
    endedAt: LocalDate? = null,
    isWorking: Boolean = false,
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
    private var mutablePositions: MutableList<Position> = mutableListOf()
    val positions: List<Position> get() = mutablePositions.toList()

    @OneToMany(mappedBy = "career", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    private var mutableSalaries: MutableList<Salary> = mutableListOf()
    val salaries: List<Salary> get() = mutableSalaries.toList()

    fun addPosition(position: Position) {
        mutablePositions.add(position)
    }

    fun addSalary(salary: Salary) {
        mutableSalaries.add(salary)
    }

    fun changeInfo(
        name: String,
        position: String,
        employmentType: EmploymentType,
        department: String,
        startedAt: LocalDate,
        endedAt: LocalDate?,
        isWorking: Boolean,
    ) {
        this.name = name
        this.position = position
        this.employmentType = employmentType
        this.department = department
        this.startedAt = startedAt
        this.endedAt = endedAt
        this.isWorking = isWorking
    }
}
