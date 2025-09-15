package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.record.RecordGroup
import com.spectrum.workfolio.domain.model.MsgKOR
import com.spectrum.workfolio.domain.repository.RecordGroupRepository
import com.spectrum.workfolio.proto.record_group.CreateRecordGroupRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordGroupService(
    private val workerService: WorkerService,
    private val recordGroupRepository: RecordGroupRepository,
    private val workerRecordGroupService: WorkerRecordGroupService,
) {

    @Transactional(readOnly = true)
    fun getRecordGroup(id: String): RecordGroup {
        return recordGroupRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_RECORD_GROUP.message) }
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
        val worker = workerService.getWorker(workerId)

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
