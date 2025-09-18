package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.record.WorkerRecordGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkerRecordGroupRepository : JpaRepository<WorkerRecordGroup, String> {
    fun findByWorkerId(workerId: String): List<WorkerRecordGroup>
    fun findByRecordGroupId(recordGroupId: String): List<WorkerRecordGroup>
    fun findByWorkerIdAndRecordGroupId(workerId: String, recordGroupId: String): WorkerRecordGroup?
    fun existsByWorkerIdAndRecordGroupId(workerId: String, recordGroupId: String): Boolean
}
