package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.record.WorkerRecordGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface WorkerRecordGroupRepository : JpaRepository<WorkerRecordGroup, String> {
    fun findByWorkerId(workerId: String): List<WorkerRecordGroup>
    
    @Query("SELECT wrg FROM WorkerRecordGroup wrg JOIN FETCH wrg.recordGroup WHERE wrg.worker.id = :workerId")
    fun findByWorkerIdWithRecordGroup(@Param("workerId") workerId: String): List<WorkerRecordGroup>
    
    fun findByRecordGroupId(recordGroupId: String): List<WorkerRecordGroup>
    fun findByWorkerIdAndRecordGroupId(workerId: String, recordGroupId: String): Optional<WorkerRecordGroup>
    fun existsByWorkerIdAndRecordGroupId(workerId: String, recordGroupId: String): Boolean
}
