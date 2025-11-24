package com.spectrum.workfolio.services.record

import com.spectrum.workfolio.domain.entity.record.RecordGroup
import com.spectrum.workfolio.domain.entity.record.WorkerRecordGroup
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.enums.RecordGroupRole
import com.spectrum.workfolio.domain.enums.RecordGroupType
import com.spectrum.workfolio.domain.repository.WorkerRecordGroupRepository
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.record_group.RecordGroupJoinRequest
import com.spectrum.workfolio.proto.record_group.SharedRecordGroupPriorityUpdateRequest
import com.spectrum.workfolio.proto.record_group.WorkerRecordGroupPriorityUpdateRequest
import com.spectrum.workfolio.services.WorkerService
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
    fun getWorkerRecordGroup(id: String): WorkerRecordGroup {
        return workerRecordGroupRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_WORKER_RECORD_GROUP.message) }
    }

    @Transactional(readOnly = true)
    fun getWorkerRecordGroup(workerId: String, recordGroupId: String): WorkerRecordGroup {
        return workerRecordGroupRepository.findByWorkerIdAndRecordGroupId(workerId, recordGroupId)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_WORKER_RECORD_GROUP.message) }
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
    fun createWorkerRecordGroup(workerId: String, recordGroup: RecordGroup, role: RecordGroupRole): WorkerRecordGroup {
        if (workerRecordGroupRepository.existsByWorkerIdAndRecordGroupId(workerId, recordGroup.id)) {
            throw WorkfolioException(MsgKOR.ALREADY_EXIST_WORKER_RECORD_GROUP.message)
        }
        val worker = workerService.getWorker(workerId)

        val workerRecordGroup = WorkerRecordGroup(
            publicId = StringUtil.generateRandomString(16),
            worker = worker,
            recordGroup = recordGroup,
            role = role,
            priority = recordGroup.priority,
        )

        return workerRecordGroupRepository.save(workerRecordGroup)
    }

    @Transactional
    fun createBulkWorkerRecordGroup(
        recordGroup: RecordGroup,
        workers: List<RecordGroupJoinRequest.JoinWorkerRequest>
    ) {
        val workerRecordGroups = workers.map {
            if (workerRecordGroupRepository.existsByWorkerIdAndRecordGroupId(it.workerId, recordGroup.id)) {
                throw WorkfolioException(MsgKOR.ALREADY_EXIST_WORKER_RECORD_GROUP.message)
            }

            val worker = workerService.getWorker(it.workerId)

            WorkerRecordGroup(
                publicId = StringUtil.generateRandomString(16),
                worker = worker,
                recordGroup = recordGroup,
                role = RecordGroupRole.valueOf(it.role.name),
                priority = recordGroup.priority,
            )
        }

        workerRecordGroupRepository.saveAll(workerRecordGroups)
    }

    @Transactional
    fun updateBulkWorkerRecordGroup(
        recordGroup: RecordGroup,
        workers: List<RecordGroupJoinRequest.JoinWorkerRequest>
    ) {
        // 기존 worker record group 조회
        val existingWorkerRecordGroups = listWorkerRecordGroupByRecordGroupId(recordGroup.id)
        
        // workers 리스트를 workerId를 키로 하는 맵으로 변환
        val workersMap = workers.associateBy { it.workerId }
        
        // 삭제할 항목과 업데이트할 항목을 분리
        val toDelete = mutableListOf<WorkerRecordGroup>()
        val toUpdate = mutableListOf<WorkerRecordGroup>()
        
        existingWorkerRecordGroups.forEach { existingWorkerRecordGroup ->
            val workerId = existingWorkerRecordGroup.worker.id
            val workerRequest = workersMap[workerId]
            
            if (workerRequest == null) {
                // workers 리스트에 없는 경우 제거 대상에 추가
                toDelete.add(existingWorkerRecordGroup)
            } else {
                // workers 리스트에 있는 경우 role 업데이트
                val newRole = RecordGroupRole.valueOf(workerRequest.role.name)
                if (existingWorkerRecordGroup.role != newRole) {
                    existingWorkerRecordGroup.changeRole(newRole)
                    toUpdate.add(existingWorkerRecordGroup)
                }
            }
        }
        
        // 일괄 삭제 및 업데이트
        if (toDelete.isNotEmpty()) {
            workerRecordGroupRepository.deleteAll(toDelete)
        }
        if (toUpdate.isNotEmpty()) {
            workerRecordGroupRepository.saveAll(toUpdate)
        }
    }

    @Transactional
    fun leaveWorkerRecordGroup(targetWorkerId: String, recordGroupId: String) {
        val workerRecordGroup = this.getWorkerRecordGroup(targetWorkerId, recordGroupId)
        workerRecordGroupRepository.delete(workerRecordGroup)
    }

    @Transactional
    fun updatePriorities(
        workerId: String,
        request: WorkerRecordGroupPriorityUpdateRequest,
    ): SuccessResponse {
        // 현재 worker가 보유한 SHARED 타입의 WorkerRecordGroup만 조회
        val myWorkerRecordGroups = listWorkerRecordGroupByWorkerId(workerId)
            .filter { it.recordGroup.type == RecordGroupType.SHARED }
        
        val myWorkerRecordGroupIds = myWorkerRecordGroups.map { it.id }.toSet()
        val prioritiesMap = request.prioritiesList.associateBy { it.workerRecordGroupId }
        
        val toUpdate = mutableListOf<WorkerRecordGroup>()
        
        prioritiesMap.forEach { (workerRecordGroupId, priorityItem) ->
            // 내가 보유한 WorkerRecordGroup인지 확인
            if (!myWorkerRecordGroupIds.contains(workerRecordGroupId)) {
                throw WorkfolioException(MsgKOR.NOT_FOUND_WORKER_RECORD_GROUP.message)
            }
            
            val workerRecordGroup = this.getWorkerRecordGroup(workerRecordGroupId)
            
            // SHARED 타입인지 확인
            if (workerRecordGroup.recordGroup.type != RecordGroupType.SHARED) {
                throw WorkfolioException(MsgKOR.NOT_FOUND_WORKER_RECORD_GROUP.message)
            }
            
            // 내 WorkerRecordGroup인지 확인
            if (workerRecordGroup.worker.id != workerId) {
                throw WorkfolioException(MsgKOR.NOT_FOUND_WORKER_RECORD_GROUP.message)
            }
            
            workerRecordGroup.changePriority(priorityItem.priority)
            toUpdate.add(workerRecordGroup)
        }
        
        if (toUpdate.isNotEmpty()) {
            workerRecordGroupRepository.saveAll(toUpdate)
        }
        
        return com.spectrum.workfolio.proto.common.SuccessResponse.newBuilder().setIsSuccess(true).build()
    }

    @Transactional
    fun updateSharedRecordGroupPriorities(
        workerId: String,
        request: SharedRecordGroupPriorityUpdateRequest,
    ): SuccessResponse {
        // 현재 worker가 보유한 SHARED 타입의 WorkerRecordGroup만 조회
        // JOIN FETCH를 사용하여 recordGroup을 함께 조회하여 lazy loading 문제 방지
        val myWorkerRecordGroups = workerRecordGroupRepository.findByWorkerIdWithRecordGroup(workerId)
            .filter { it.recordGroup.type == RecordGroupType.SHARED }
        
        val myWorkerRecordGroupsByRecordGroupId = myWorkerRecordGroups
            .associateBy { it.recordGroup.id }
        
        val prioritiesMap = request.prioritiesList.associateBy { it.recordGroupId }
        val toUpdate = mutableListOf<WorkerRecordGroup>()

        prioritiesMap.forEach { (recordGroupId, priorityItem) ->
            val workerRecordGroup = myWorkerRecordGroupsByRecordGroupId[recordGroupId]
                ?: throw WorkfolioException(MsgKOR.NOT_FOUND_WORKER_RECORD_GROUP.message)

            // SHARED 타입인지 확인
            if (workerRecordGroup.recordGroup.type != RecordGroupType.SHARED) {
                throw WorkfolioException(MsgKOR.NOT_FOUND_RECORD_GROUP.message)
            }

            // 내 WorkerRecordGroup인지 확인
            if (workerRecordGroup.worker.id != workerId) {
                throw WorkfolioException(MsgKOR.NOT_FOUND_WORKER_RECORD_GROUP.message)
            }

            workerRecordGroup.changePriority(priorityItem.priority)
            toUpdate.add(workerRecordGroup)
        }

        if (toUpdate.isNotEmpty()) {
            workerRecordGroupRepository.saveAll(toUpdate)
        }

        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
