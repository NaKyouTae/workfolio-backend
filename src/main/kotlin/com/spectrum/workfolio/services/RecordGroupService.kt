package com.spectrum.workfolio.services

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.domain.entity.record.RecordGroup
import com.spectrum.workfolio.domain.model.MsgKOR
import com.spectrum.workfolio.domain.repository.RecordGroupRepository
import com.spectrum.workfolio.proto.record_group.CreateRecordGroupRequest
import com.spectrum.workfolio.proto.record_group.JoinRecordGroupRequest
import com.spectrum.workfolio.proto.record_group.UpdateRecordGroupRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody

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

    @Transactional
    fun updateRecordGroup(
        workerId: String,
        recordGroupId: String,
        request: UpdateRecordGroupRequest
    ): RecordGroup {
        val recordGroup = this.getRecordGroup(recordGroupId)
        val workerRecordGroup = workerRecordGroupService.getWorkerRecordGroup(workerId, recordGroupId)

        if(workerRecordGroup != null) {
            throw WorkfolioException(MsgKOR.NOT_MATCH_RECORD_GROUP_EDITOR.message)
        }

        recordGroup.changeRecordGroup(request.title, request.color, request.isPublic, request.priority)

        return recordGroupRepository.save(recordGroup)
    }

    @Transactional
    fun joinRecordGroup(workerId: String, request: JoinRecordGroupRequest): RecordGroup {
        val recordGroup = this.getRecordGroup(request.recordGroupId)

        if(recordGroup.worker.id != workerId) {
            throw WorkfolioException(MsgKOR.NOT_MATCH_RECORD_GROUP_OWNER.message)
        }

        workerRecordGroupService.createWorkerRecordGroup(request.targetWorkerId, recordGroup)

        return recordGroup
    }
}
