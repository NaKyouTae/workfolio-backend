package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.entity.uitemplate.UITemplate
import com.spectrum.workfolio.domain.entity.uitemplate.WorkerUITemplate
import com.spectrum.workfolio.domain.enums.UITemplateType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface WorkerUITemplateRepository : JpaRepository<WorkerUITemplate, String> {
    @Query("""
        SELECT wt FROM WorkerUITemplate wt
        JOIN FETCH wt.worker
        JOIN FETCH wt.uiTemplate
        WHERE wt.worker = :worker AND wt.isActive = true
    """)
    fun findByWorkerAndIsActiveTrue(@Param("worker") worker: Worker): List<WorkerUITemplate>

    @Query("""
        SELECT wt FROM WorkerUITemplate wt
        JOIN FETCH wt.worker
        JOIN FETCH wt.uiTemplate
        WHERE wt.worker = :worker AND wt.isActive = true
        ORDER BY wt.purchasedAt DESC
    """,
    countQuery = """
        SELECT COUNT(wt) FROM WorkerUITemplate wt
        WHERE wt.worker = :worker AND wt.isActive = true
    """)
    fun findByWorkerAndIsActiveTrueOrderByPurchasedAtDesc(@Param("worker") worker: Worker, pageable: Pageable): Page<WorkerUITemplate>

    @Query("""
        SELECT wt FROM WorkerUITemplate wt
        JOIN FETCH wt.worker
        JOIN FETCH wt.uiTemplate
        WHERE wt.worker = :worker AND wt.uiTemplate = :uiTemplate AND wt.isActive = true
    """)
    fun findByWorkerAndUiTemplateAndIsActiveTrue(@Param("worker") worker: Worker, @Param("uiTemplate") uiTemplate: UITemplate): WorkerUITemplate?

    @Query("""
        SELECT wt FROM WorkerUITemplate wt
        JOIN FETCH wt.worker
        JOIN FETCH wt.uiTemplate
        WHERE wt.worker = :worker
        AND wt.isActive = true
        AND wt.expiredAt > :now
    """)
    fun findActiveByWorker(
        @Param("worker") worker: Worker,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): List<WorkerUITemplate>

    @Query("""
        SELECT wt FROM WorkerUITemplate wt
        JOIN FETCH wt.worker
        JOIN FETCH wt.uiTemplate
        WHERE wt.worker = :worker
        AND wt.uiTemplate = :uiTemplate
        AND wt.isActive = true
        AND wt.expiredAt > :now
    """)
    fun findValidByWorkerAndUITemplate(
        @Param("worker") worker: Worker,
        @Param("uiTemplate") uiTemplate: UITemplate,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): WorkerUITemplate?

    @Query("""
        SELECT wt FROM WorkerUITemplate wt
        JOIN FETCH wt.worker
        JOIN FETCH wt.uiTemplate t
        WHERE wt.worker = :worker
        AND t.type = :type
        AND wt.isActive = true
        AND wt.expiredAt > :now
    """)
    fun findValidByWorkerAndType(
        @Param("worker") worker: Worker,
        @Param("type") type: UITemplateType,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): List<WorkerUITemplate>

    @Query("""
        SELECT COUNT(wt) > 0 FROM WorkerUITemplate wt
        WHERE wt.worker = :worker
        AND wt.uiTemplate = :uiTemplate
        AND wt.isActive = true
        AND wt.expiredAt > :now
    """)
    fun hasValidUITemplate(
        @Param("worker") worker: Worker,
        @Param("uiTemplate") uiTemplate: UITemplate,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): Boolean

    fun deleteByUiTemplateId(uiTemplateId: String): Long
}
