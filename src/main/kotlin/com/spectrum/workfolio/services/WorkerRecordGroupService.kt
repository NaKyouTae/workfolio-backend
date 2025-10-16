package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.record.RecordGroup
import com.spectrum.workfolio.domain.entity.record.WorkerRecordGroup
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.WorkerRecordGroupRepository
import com.spectrum.workfolio.utils.StringUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WorkerRecordGroupService(
    private val workerService: WorkerService,
    private val workerRecordGroupRepository: WorkerRecordGroupRepository,
) {
    @Transactional(readOnly = true)
    fun getWorkerRecordGroup(workerId: String, recordGroupId: String): WorkerRecordGroup? {
        return workerRecordGroupRepository.findByWorkerIdAndRecordGroupId(workerId, recordGroupId)
    }

    @Transactional(readOnly = true)
    fun listWorkerRecordGroupByRecordGroupId(recordGroupId: String): List<WorkerRecordGroup> {
        return workerRecordGroupRepository.findByRecordGroupId(recordGroupId)
    }

    @Transactional(readOnly = true)
    fun listWorkerRecordGroupByWorkerId(workerId: String): List<WorkerRecordGroup> {
        return workerRecordGroupRepository.findByWorkerId(workerId)
    }

    @Transactional(readOnly = true)
    fun listWorkerRecordGroupForEditable(workerId: String): List<WorkerRecordGroup> {
        val editableRecordGroup = workerRecordGroupRepository.findByWorkerId(workerId)
        // TODO 편집 가능한 권한이 있는 Record Group만 조회하도록 수정 필요
        return editableRecordGroup
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

    @Transactional
    fun deleteWorkerRecordGroupAll(recordGroupId: String) {
        val workerRecordGroups = this.listWorkerRecordGroupByRecordGroupId(recordGroupId)
        val ids = workerRecordGroups.map { it.id }
        workerRecordGroupRepository.deleteAllById(ids)
    }
}
