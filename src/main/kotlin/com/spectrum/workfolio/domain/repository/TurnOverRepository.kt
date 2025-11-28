package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.turnover.TurnOver
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface TurnOverRepository : JpaRepository<TurnOver, String> {
    fun findByWorkerId(workerId: String): List<TurnOver>
    
    // MultipleBagFetchException 방지: JOIN FETCH 없이 기본 조회만 수행
    // 컬렉션은 별도 쿼리로 가져오거나 @BatchSize로 처리
    @Query("""
        SELECT t FROM TurnOver t
        WHERE t.worker.id = :workerId
        ORDER BY t.createdAt DESC
    """)
    fun findByWorkerIdWithCollections(@Param("workerId") workerId: String): List<TurnOver>
}
