package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.record.RecordGroup
import com.spectrum.workfolio.domain.repository.RecordGroupRepository
import com.spectrum.workfolio.proto.CreateRecordGroupRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class RecordGroupService(
    private val workerService: WorkerService,
    private val recordGroupRepository: RecordGroupRepository,
) {

    @Transactional(readOnly = true)
    fun getRecordGroup(id: String): Optional<RecordGroup> {
        return recordGroupRepository.findById(id)
    }

    @Transactional(readOnly = true)
    fun listRecordGroups(workerId: String): List<RecordGroup> {
        return recordGroupRepository.findByWorkerIdOrderByPriorityDesc(workerId)
    }

    @Transactional
    fun createRecordGroup(
        workerId: String,
        params: CreateRecordGroupRequest
    ): RecordGroup {
        val worker = workerService.getWorker(workerId).orElseThrow { RuntimeException("Worker not found") }

        return recordGroupRepository.save(
            RecordGroup(
                title = params.title,
                color = params.color,
                isPublic = false,
                publicId = RecordGroup.generateShortPublicId(),
                priority = params.priority,
                worker = worker,
            )
        )
    }
}
