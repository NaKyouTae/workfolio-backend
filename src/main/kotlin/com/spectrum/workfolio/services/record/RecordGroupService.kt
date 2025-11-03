package com.spectrum.workfolio.services.record

import com.spectrum.workfolio.domain.entity.record.RecordGroup
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.enums.RecordGroupType
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.RecordGroupRepository
import com.spectrum.workfolio.proto.record_group.CreateRecordGroupRequest
import com.spectrum.workfolio.proto.record_group.RecordGroupDetailResponse
import com.spectrum.workfolio.proto.record_group.RecordGroupJoinRequest
import com.spectrum.workfolio.proto.record_group.RecordGroupResponse
import com.spectrum.workfolio.proto.record_group.RecordGroupUpdateRequest
import com.spectrum.workfolio.services.WorkerService
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
        return recordGroupRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_RECORD_GROUP.message) }
    }

    @Transactional(readOnly = true)
    fun getPublicRecordGroup(publicId: String): RecordGroup {
        return recordGroupRepository.findByPublicId(publicId)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_RECORD_GROUP.message) }
    }

    @Transactional(readOnly = true)
    fun listOwnedRecordGroups(workerId: String): List<com.spectrum.workfolio.proto.common.RecordGroup> {
        val recordGroups =
            recordGroupRepository.findByWorkerIdAndTypeOrderByPriorityDesc(workerId, RecordGroupType.PRIVATE)
        return recordGroups.map { it.toProto() }
    }

    @Transactional(readOnly = true)
    fun listSharedRecordGroups(workerId: String): List<com.spectrum.workfolio.proto.common.RecordGroup> {
        val workerRecordGroups = workerRecordGroupService.listWorkerRecordGroupByWorkerId(workerId)
        val recordGroups = workerRecordGroups.map { it -> it.recordGroup }.sortedBy { it.priority }
        return recordGroups.map { it.toProto() }
    }

    @Transactional(readOnly = true)
    fun listEditableRecordGroups(workerId: String): List<com.spectrum.workfolio.proto.common.RecordGroup> {
        val ownedRecordGroup = this.listOwnedRecordGroups(workerId)
        val editableWorkerRecordGroup = workerRecordGroupService.listWorkerRecordGroupForEditable(workerId)
        val editableRecordGroups = editableWorkerRecordGroup.map { it -> it.recordGroup.toProto() }

        return ownedRecordGroup + editableRecordGroups
    }

    @Transactional(readOnly = true)
    fun listRecordGroupDetailResult(id: String): RecordGroupDetailResponse {
        val recordGroup = this.getRecordGroup(id)
        val workerRecordGroups = recordGroup.workerRecordGroups
        val workers = workerRecordGroups.map { it.worker.toProto() }

        return RecordGroupDetailResponse.newBuilder()
            .setGroups(recordGroup.toProto())
            .addAllWorkers(workers)
            .build()
    }

    @Transactional
    fun createRecordGroup(
        workerId: String,
        isDefault: Boolean = false,
        request: CreateRecordGroupRequest,
    ): RecordGroupResponse {
        val worker = workerService.getWorker(workerId)
        val recordGroup = RecordGroup(
            type = RecordGroupType.valueOf(request.type.name),
            title = request.title,
            color = request.color,
            isDefault = isDefault,
            publicId = RecordGroup.generateShortPublicId(),
            priority = request.priority,
            worker = worker,
        )

        val createdRecordGroup = recordGroupRepository.save(recordGroup)

        if (createdRecordGroup.type == RecordGroupType.SHARED) {
            workerRecordGroupService.createWorkerRecordGroup(workerId, recordGroup)
        }

        return RecordGroupResponse.newBuilder().setRecordGroup(createdRecordGroup.toProto()).build()
    }

    @Transactional
    fun updateRecordGroup(
        recordGroupId: String,
        request: RecordGroupUpdateRequest,
    ): RecordGroupResponse {
        val recordGroup = this.getRecordGroup(recordGroupId)

        recordGroup.changeRecordGroup(request.title, request.color, request.priority)

        val updatedRecordGroup = recordGroupRepository.save(recordGroup)

        return RecordGroupResponse.newBuilder().setRecordGroup(updatedRecordGroup.toProto()).build()
    }

    @Transactional
    fun joinRecordGroup(workerId: String, request: RecordGroupJoinRequest): RecordGroup {
        val recordGroup = this.getRecordGroup(request.recordGroupId)

        // 소유주만 멤버를 추가할수 있다.
        // TODO 권한이 생긴다면 권한에 맞게 멤버 추가 권한을 체크하는 로직 필요
        if (recordGroup.worker.id != workerId) {
            throw WorkfolioException(MsgKOR.NOT_MATCH_RECORD_GROUP_OWNER.message)
        }

        // 소유주가 가지고 있는 레코드 그룹에 멤버로 소유주가 들어가는 경우 방지
        if (recordGroup.worker.id == request.workerId) {
            throw WorkfolioException(MsgKOR.ALREADY_EXIST_WORKER_RECORD_GROUP.message)
        }

        recordGroup.changeType(RecordGroupType.SHARED)

        workerRecordGroupService.createWorkerRecordGroup(request.workerId, recordGroup)

        return recordGroup
    }

    @Transactional
    fun deleteRecordGroup(
        workerId: String,
        recordGroupId: String,
    ) {
        val recordGroup = this.getRecordGroup(recordGroupId)

        if (recordGroup.worker.id != workerId) {
            throw WorkfolioException(MsgKOR.NOT_OWNER_RECORD_GROUP.message)
        }

        if (recordGroup.isDefault) {
            throw WorkfolioException(MsgKOR.CANNOT_DELETED_DEFAULT_RECORD_GROUP.message)
        }

        recordGroupRepository.delete(recordGroup)
    }
}
