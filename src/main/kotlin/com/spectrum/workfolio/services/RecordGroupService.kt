package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.record.RecordGroup
import com.spectrum.workfolio.domain.extensions.toProtoResponse
import com.spectrum.workfolio.domain.extensions.toRecordGroupProto
import com.spectrum.workfolio.domain.model.MsgKOR
import com.spectrum.workfolio.domain.repository.RecordGroupRepository
import com.spectrum.workfolio.proto.record_group.CreateRecordGroupRequest
import com.spectrum.workfolio.proto.record_group.CreateRecordGroupResponse
import com.spectrum.workfolio.proto.record_group.JoinRecordGroupRequest
import com.spectrum.workfolio.proto.record_group.UpdateRecordGroupRequest
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
    fun listOwnedRecordGroups(workerId: String): List<com.spectrum.workfolio.proto.common.RecordGroup> {
        val recordGroups = recordGroupRepository.findByWorkerIdOrderByPriorityDesc(workerId)
        return recordGroups.map { it.toRecordGroupProto() }
    }

    @Transactional(readOnly = true)
    fun listSharedRecordGroups(workerId: String): List<com.spectrum.workfolio.proto.common.RecordGroup> {
        val workerRecordGroups = workerRecordGroupService.listWorkerRecordGroup(workerId)
        val recordGroups = workerRecordGroups.map { it -> it.recordGroup }.sortedBy { it.priority }
        return recordGroups.map { it.toRecordGroupProto() }
    }

    @Transactional(readOnly = true)
    fun listEditableRecordGroups(workerId: String): List<com.spectrum.workfolio.proto.common.RecordGroup> {
        val ownedRecordGroup = this.listOwnedRecordGroups(workerId)
        val editableWorkerRecordGroup = workerRecordGroupService.listWorkerRecordGroupForEditable(workerId)
        val editableRecordGroups = editableWorkerRecordGroup.map { it -> it.recordGroup.toRecordGroupProto() }

        return ownedRecordGroup + editableRecordGroups
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

        // 소유주만 멤버를 추가할수 있다.
        // TODO 권한이 생긴다면 권한에 맞게 멤버 추가 권한을 체크하는 로직 필요
        if(recordGroup.worker.id != workerId) {
            throw WorkfolioException(MsgKOR.NOT_MATCH_RECORD_GROUP_OWNER.message)
        }

        // 소유주가 가지고 있는 레코드 그룹에 멤버로 소유주가 들어가는 경우 방지
        if(recordGroup.worker.id == request.targetWorkerId) {
            throw WorkfolioException(MsgKOR.ALREADY_EXIST_WORKER_RECORD_GROUP.message)
        }

        workerRecordGroupService.createWorkerRecordGroup(request.targetWorkerId, recordGroup)

        return recordGroup
    }
}
