package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.record.Record
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface RecordRepository : JpaRepository<Record, String> {
    @Query(
        """
       SELECT r 
          FROM Record r 
       WHERE r.recordGroup.id in (:recordGroupIds) 
          AND r.startedAt >= :startDate 
          AND r.startedAt < :endDate 
    """,
    )
    fun findByDateRange(
        @Param("recordGroupIds") recordGroupIds: List<String>,
        @Param("startDate") startDate: LocalDateTime,
        @Param("endDate") endDate: LocalDateTime,
    ): List<Record>

    @Query(
        """
       SELECT r
         FROM Record r
        WHERE r.recordGroup.id in (:recordGroupIds)
    """,
    )
    fun findAllByRecordGroupIds(
        @Param("recordGroupIds") recordGroupIds: List<String>,
        pageable: Pageable,
    ): Page<Record>

    // Full-Text Search - 기본 검색
    @Query(
        value = """
        SELECT * FROM records 
        WHERE record_group_id in (:recordGroupIds) 
        AND search_vector @@ plainto_tsquery('simple', :keyword)
        ORDER BY created_at DESC
    """,
        nativeQuery = true,
    )
    fun searchByFullText(recordGroupIds: List<String>, keyword: String): List<Record>

    // Full-Text Search - 관련성 순위로 정렬
    @Query(
        value = """
        SELECT *, ts_rank(search_vector, plainto_tsquery('simple', :keyword)) as rank
        FROM records 
        WHERE worker_id = :workerId 
        AND search_vector @@ plainto_tsquery('simple', :keyword)
        ORDER BY rank DESC, created_at DESC
    """,
        nativeQuery = true,
    )
    fun searchByFullTextRanked(workerId: String, keyword: String): List<Record>

    // Full-Text Search - 모든 사용자 (관리자용)
    @Query(
        value = """
        SELECT * FROM records 
        WHERE search_vector @@ plainto_tsquery('simple', :keyword)
        AND is_public = true
        ORDER BY created_at DESC
        LIMIT :limit
    """,
        nativeQuery = true,
    )
    fun searchPublicRecords(keyword: String, limit: Int = 20): List<Record>
}
