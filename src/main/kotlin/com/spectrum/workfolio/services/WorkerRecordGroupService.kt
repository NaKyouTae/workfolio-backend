package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.record.RecordGroup
import com.spectrum.workfolio.domain.entity.record.WorkerRecordGroup
import com.spectrum.workfolio.domain.model.MsgKOR
import com.spectrum.workfolio.domain.repository.WorkerRecordGroupRepository
import com.spectrum.workfolio.proto.record_group.JoinRecordGroupRequest
import com.spectrum.workfolio.utils.StringUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WorkerRecordGroupService(
    private val workerService: WorkerService,
    private val recordGroupService: RecordGroupService,
    private val workerRecordGroupRepository: WorkerRecordGroupRepository,
) {
    @Transactional
    fun joinRecordGroup(workerId: String, request: JoinRecordGroupRequest): RecordGroup {
        val recordGroup = recordGroupService.getRecordGroup(request.recordGroupId)

        if(recordGroup.worker.id != workerId) {
            throw WorkfolioException(MsgKOR.NOT_MATCH_RECORD_GROUP_OWNER.message)
        }

        this.createWorkerRecordGroup(request.targetWorkerId, recordGroup)

        return recordGroup
    }

    @Transactional(readOnly = true)
    fun getWorkerRecordGroup(workerId: String, recordGroupId: String): WorkerRecordGroup? {
        return workerRecordGroupRepository.findByWorkerIdAndRecordGroupId(workerId, recordGroupId)
    }

    @Transactional
    fun createWorkerRecordGroup(workerId: String, recordGroup: RecordGroup): WorkerRecordGroup {
        if (workerRecordGroupRepository.existsByWorkerIdAndRecordGroupId(workerId, recordGroup.id)) {
            throw WorkfolioException(MsgKOR.ALREADY_EXIST_WORKER_RECORD_GROUP.message)
        }
        val worker = workerService.getWorker(workerId)

        val workerRecordGroup = WorkerRecordGroup(
            publicId = StringUtil.generateRandomString(16),
            worker = worker,
            recordGroup = recordGroup,
        )

        return workerRecordGroupRepository.save(workerRecordGroup)
    }
}
