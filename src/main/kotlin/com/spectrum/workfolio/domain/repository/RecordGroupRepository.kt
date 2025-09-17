package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.record.RecordGroup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface RecordGroupRepository: JpaRepository<RecordGroup, String> {
    fun findByWorkerIdOrderByPriorityDesc(workerId: String): List<RecordGroup>
    fun findByPublicId(publicId: String): Optional<RecordGroup>
}
