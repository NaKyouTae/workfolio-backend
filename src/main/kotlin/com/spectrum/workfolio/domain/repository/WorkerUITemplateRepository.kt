package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.entity.uitemplate.UITemplate
import com.spectrum.workfolio.domain.entity.uitemplate.WorkerUITemplate
import com.spectrum.workfolio.domain.enums.UITemplateType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
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
        WHERE wt.worker = :worker AND wt.status = com.spectrum.workfolio.domain.enums.WorkerUITemplateStatus.ACTIVE
    """)
    fun findByWorkerAndStatusActive(@Param("worker") worker: Worker): List<WorkerUITemplate>

    @Query("""
        SELECT wt FROM WorkerUITemplate wt
        JOIN FETCH wt.worker
        JOIN FETCH wt.uiTemplate
        WHERE wt.worker = :worker AND wt.status = com.spectrum.workfolio.domain.enums.WorkerUITemplateStatus.ACTIVE
        ORDER BY wt.purchasedAt DESC
    """,
    countQuery = """
        SELECT COUNT(wt) FROM WorkerUITemplate wt
        WHERE wt.worker = :worker AND wt.status = com.spectrum.workfolio.domain.enums.WorkerUITemplateStatus.ACTIVE
    """)
    fun findByWorkerAndStatusActiveOrderByPurchasedAtDesc(@Param("worker") worker: Worker, pageable: Pageable): Page<WorkerUITemplate>

    @Query("""
        SELECT wt FROM WorkerUITemplate wt
        JOIN FETCH wt.worker
        JOIN FETCH wt.uiTemplate
        WHERE wt.worker = :worker AND wt.uiTemplate = :uiTemplate AND wt.status = com.spectrum.workfolio.domain.enums.WorkerUITemplateStatus.ACTIVE
    """)
    fun findByWorkerAndUiTemplateAndStatusActive(@Param("worker") worker: Worker, @Param("uiTemplate") uiTemplate: UITemplate): WorkerUITemplate?

    @Query("""
        SELECT wt FROM WorkerUITemplate wt
        JOIN FETCH wt.worker
        JOIN FETCH wt.uiTemplate
        WHERE wt.worker = :worker
        AND wt.status = com.spectrum.workfolio.domain.enums.WorkerUITemplateStatus.ACTIVE
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
        AND wt.status = com.spectrum.workfolio.domain.enums.WorkerUITemplateStatus.ACTIVE
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
        AND wt.status = com.spectrum.workfolio.domain.enums.WorkerUITemplateStatus.ACTIVE
        AND wt.expiredAt > :now
    """)
    fun findValidByWorkerAndType(
        @Param("worker") worker: Worker,
        @Param("type") type: UITemplateType,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): List<WorkerUITemplate>

    @Query("""
        SELECT wt FROM WorkerUITemplate wt
        JOIN FETCH wt.worker
        JOIN FETCH wt.uiTemplate
        WHERE wt.worker = :worker
        AND wt.uiTemplate = :uiTemplate
        AND wt.status = com.spectrum.workfolio.domain.enums.WorkerUITemplateStatus.ACTIVE
        AND wt.expiredAt <= :now
        ORDER BY wt.expiredAt DESC
        LIMIT 1
    """)
    fun findExpiredByWorkerAndUITemplate(
        @Param("worker") worker: Worker,
        @Param("uiTemplate") uiTemplate: UITemplate,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): WorkerUITemplate?

    @Query("""
        SELECT COUNT(wt) > 0 FROM WorkerUITemplate wt
        WHERE wt.worker = :worker
        AND wt.uiTemplate = :uiTemplate
        AND wt.status = com.spectrum.workfolio.domain.enums.WorkerUITemplateStatus.ACTIVE
        AND wt.expiredAt > :now
    """)
    fun hasValidUITemplate(
        @Param("worker") worker: Worker,
        @Param("uiTemplate") uiTemplate: UITemplate,
        @Param("now") now: LocalDateTime = LocalDateTime.now()
    ): Boolean

    fun deleteByUiTemplateId(uiTemplateId: String): Long

    @Query("""
        SELECT wt FROM WorkerUITemplate wt
        JOIN FETCH wt.uiTemplate
        WHERE wt.worker = :worker
        AND wt.templateType = :templateType
        AND wt.isDefault = true
        AND wt.status = com.spectrum.workfolio.domain.enums.WorkerUITemplateStatus.ACTIVE
    """)
    fun findDefaultByWorkerAndType(
        @Param("worker") worker: Worker,
        @Param("templateType") templateType: UITemplateType,
    ): WorkerUITemplate?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE WorkerUITemplate wt
        SET wt.isDefault = false
        WHERE wt.worker = :worker
        AND wt.templateType = :templateType
        AND wt.isDefault = true
    """)
    fun clearDefaultByWorkerAndType(
        @Param("worker") worker: Worker,
        @Param("templateType") templateType: UITemplateType,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE WorkerUITemplate wt
        SET wt.isDefault = false
        WHERE wt.uiTemplate.id = :uiTemplateId
        AND wt.isDefault = true
    """)
    fun clearDefaultByUiTemplateId(
        @Param("uiTemplateId") uiTemplateId: String,
    ): Int
}
