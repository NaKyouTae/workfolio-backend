package com.spectrum.workfolio.domain.entity.turnover

import com.spectrum.workfolio.domain.enums.EmploymentType
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import java.time.LocalDate

@Embeddable
class TurnOverRetrospective(
    name: String, // 회사명
    salary: Int, // 연봉
    position: String, // 직무
    jobTitle: String, // 직책
    rank: String, // 직급
    department: String, // 부서
    reason: String, // 선택 사유
    score: Int, // 점수
    reviewSummary: String, // 한줄 회고
    joinedAt: LocalDate? = null, // 입사년월
    workType: String, // 근무 형태
    employmentType: EmploymentType? = null, // 재직 형태
) {
    @Column(name = "retrospective_name", length = 1024, nullable = false)
    var name: String = name
        protected set

    @Column(name = "retrospective_salary", nullable = false)
    var salary: Int = salary
        protected set

    @Column(name = "retrospective_position", length = 512, nullable = false)
    var position: String = position
        protected set

    @Column(name = "retrospective_job_title", length = 512, nullable = false)
    var jobTitle: String = jobTitle
        protected set

    @Column(name = "retrospective_rank", length = 512, nullable = false)
    var rank: String = rank
        protected set

    @Column(name = "retrospective_department", length = 512, nullable = false)
    var department: String = department
        protected set

    @Column(name = "retrospective_reason", columnDefinition = "TEXT", nullable = false)
    var reason: String = reason
        protected set

    @Column(name = "retrospective_score", nullable = false)
    var score: Int = score
        protected set

    @Column(name = "retrospective_review_summary", columnDefinition = "TEXT", nullable = false)
    var reviewSummary: String = reviewSummary
        protected set

    @Column(name = "retrospective_joined_at", nullable = true)
    var joinedAt: LocalDate? = joinedAt
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "retrospective_employment_type", length = 32, nullable = true)
    var employmentType: EmploymentType? = employmentType
        protected set

    @Column(name = "retrospective_work_type", length = 512, nullable = false)
    var workType: String = workType
        protected set

    fun changeInfo(
        name: String, // 회사명
        salary: Int, // 연봉
        position: String, // 직무
        jobTitle: String, // 직책
        rank: String, // 직급
        department: String, // 부서
        reason: String, // 선택 사유
        score: Int, // 점수
        reviewSummary: String, // 한줄 회고
        joinedAt: LocalDate? = null, // 입사년월
        workType: String, // 근무 형태
        employmentType: EmploymentType? = null, // 재직 형태
    ) {
        this.name = name
        this.salary = salary
        this.position = position
        this.jobTitle = jobTitle
        this.rank = rank
        this.department = department
        this.reason = reason
        this.score = score
        this.reviewSummary = reviewSummary
        this.joinedAt = joinedAt
        this.workType = workType
        this.employmentType = employmentType
    }
}
