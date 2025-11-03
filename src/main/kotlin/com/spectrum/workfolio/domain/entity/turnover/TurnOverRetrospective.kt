package com.spectrum.workfolio.domain.entity.turnover

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.enums.EmploymentType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(
    name = "turn_over_retrospectives",
    indexes = [
        Index(name = "idx_turn_over_retrospectives_turn_over_id", columnList = "turn_over_id"),
    ],
)
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
    turnOver: TurnOver,
) : BaseEntity("TR") {
    @Column(name = "name", length = 1024, nullable = false)
    var name: String = name
        protected set

    @Column(name = "salary", nullable = false)
    var salary: Int = salary
        protected set

    @Column(name = "position", length = 512, nullable = false)
    var position: String = position
        protected set

    @Column(name = "job_title", length = 512, nullable = false)
    var jobTitle: String = jobTitle
        protected set

    @Column(name = "rank", length = 512, nullable = false)
    var rank: String = rank
        protected set

    @Column(name = "department", length = 512, nullable = false)
    var department: String = department
        protected set

    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    var reason: String = reason
        protected set

    @Column(name = "score", nullable = false)
    var score: Int = score
        protected set

    @Column(name = "review_summary", columnDefinition = "TEXT", nullable = false)
    var reviewSummary: String = reviewSummary
        protected set

    @Column(name = "joined_at", nullable = true)
    var joinedAt: LocalDate? = joinedAt
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", length = 32, nullable = true)
    var employmentType: EmploymentType? = employmentType
        protected set

    @Column(name = "work_type", length = 512, nullable = false)
    var workType: String = workType
        protected set

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turn_over_id", nullable = false)
    var turnOver: TurnOver = turnOver
        protected set
}
