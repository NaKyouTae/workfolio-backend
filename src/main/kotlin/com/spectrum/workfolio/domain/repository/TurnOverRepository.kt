package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.turnover.TurnOver
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TurnOverRepository : JpaRepository<TurnOver, String> {
    fun findByWorkerId(workerId: String): List<TurnOver>
    
    @Query("""
        SELECT DISTINCT t FROM TurnOver t
        LEFT JOIN FETCH t.mutableSelfIntroductions
        LEFT JOIN FETCH t.mutableInterviewQuestions
        LEFT JOIN FETCH t.mutableCheckList
        LEFT JOIN FETCH t.mutableJobApplications
        WHERE t.worker.id = :workerId
    """)
    fun findByWorkerIdWithCollections(@Param("workerId") workerId: String): List<TurnOver>
}
