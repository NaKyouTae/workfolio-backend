package com.spectrum.workfolio.services.record

import com.spectrum.workfolio.domain.entity.record.RecordGroup
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.enums.RecordGroupRole
import com.spectrum.workfolio.domain.enums.RecordGroupType
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.RecordGroupRepository
import com.spectrum.workfolio.proto.common.SuccessResponse
import com.spectrum.workfolio.proto.record_group.CreateRecordGroupRequest
import com.spectrum.workfolio.proto.record_group.RecordGroupDetailResponse
import com.spectrum.workfolio.proto.record_group.RecordGroupJoinRequest
import com.spectrum.workfolio.proto.record_group.RecordGroupPriorityUpdateRequest
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
        val sortedRecordGroups = recordGroups.sortedBy { it.priority }
        return sortedRecordGroups.map { it.toProto() }
    }

    @Transactional(readOnly = true)
    fun listSharedRecordGroups(workerId: String): List<com.spectrum.workfolio.proto.common.RecordGroup> {
        val workerRecordGroups = workerRecordGroupService.listWorkerRecordGroupByWorkerId(workerId)
        val sortedWorkerRecordGroups = workerRecordGroups.sortedBy { it.priority }
        val recordGroups = sortedWorkerRecordGroups.map { it.recordGroup }
        return recordGroups.map { it.toProto() }
    }

    @Transactional(readOnly = true)
    fun listEditableRecordGroups(workerId: String): List<com.spectrum.workfolio.proto.common.RecordGroup> {
        val ownedRecordGroup = this.listOwnedRecordGroups(workerId)
        val editableWorkerRecordGroup = workerRecordGroupService.listWorkerRecordGroupForEditable(workerId)
        val editableRecordGroups = editableWorkerRecordGroup.map { it.recordGroup.toProto() }

        return ownedRecordGroup + editableRecordGroups
    }

    @Transactional(readOnly = true)
    fun listRecordGroupDetailResult(id: String): RecordGroupDetailResponse {
        val recordGroup = this.getRecordGroup(id)
        val workerRecordGroups = recordGroup.workerRecordGroups
        val workers = workerRecordGroups.map { it.toProto() }

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
            defaultRole = RecordGroupRole.VIEW,
            worker = worker,
        )

        val createdRecordGroup = recordGroupRepository.save(recordGroup)

        if (createdRecordGroup.type == RecordGroupType.SHARED) {
            workerRecordGroupService.createWorkerRecordGroup(
                workerId,
                recordGroup,
                RecordGroupRole.ADMIN
            )
        }

        val build = RecordGroupResponse.newBuilder().setRecordGroup(createdRecordGroup.toProto()).build()

        return build
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
    fun joinRecordGroup(workerId: String, request: RecordGroupJoinRequest): RecordGroupResponse {
        val recordGroup = this.getRecordGroup(request.id)

        // 소유주만 멤버를 추가할수 있다.
        if (recordGroup.worker.id != workerId) {
            throw WorkfolioException(MsgKOR.NOT_MATCH_RECORD_GROUP_OWNER.message)
        }

        val allWorkerList = request.existWorkersList + request.newWorkersList
        val masterWorker =
            allWorkerList.find { it.role == com.spectrum.workfolio.proto.common.WorkerRecordGroup.RecordGroupRole.ADMIN }

        if (masterWorker != null && recordGroup.worker.id != masterWorker.workerId) {
            val newMasterWorker = workerService.getWorker(masterWorker.workerId)
            recordGroup.changeWorker(newMasterWorker)
        }

        recordGroup.changeType(
            request.title,
            request.color,
            RecordGroupRole.valueOf(request.defaultRole.name),
            RecordGroupType.valueOf(request.type.name),
        )

        workerRecordGroupService.updateBulkWorkerRecordGroup(recordGroup, request.existWorkersList)
        workerRecordGroupService.createBulkWorkerRecordGroup(recordGroup, request.newWorkersList)

        return RecordGroupResponse.newBuilder().setRecordGroup(recordGroup.toProto()).build()
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

    @Transactional
    fun updatePriorities(
        workerId: String,
        request: RecordGroupPriorityUpdateRequest,
    ): SuccessResponse {
        val type = RecordGroupType.valueOf(request.type.name)
        val prioritiesMap = request.prioritiesList.associateBy { it.recordGroupId }

        when (type) {
            RecordGroupType.PRIVATE -> {
                // PRIVATE 타입: 소유주만 우선순위 변경 가능
                val recordGroups = recordGroupRepository.findByWorkerIdAndTypeOrderByPriorityDesc(workerId, type)
                val toUpdate = mutableListOf<RecordGroup>()

                recordGroups.forEach { recordGroup ->
                    val priorityItem = prioritiesMap[recordGroup.id]
                    if (priorityItem != null) {
                        // 소유주 확인
                        if (recordGroup.worker.id != workerId) {
                            throw WorkfolioException(MsgKOR.NOT_OWNER_RECORD_GROUP.message)
                        }
                        recordGroup.changePriority(priorityItem.priority)
                        toUpdate.add(recordGroup)
                    }
                }

                if (toUpdate.isNotEmpty()) {
                    recordGroupRepository.saveAll(toUpdate)
                }
            }
            RecordGroupType.SHARED -> {
                // SHARED 타입은 WorkerRecordGroupController의 API를 사용
                throw WorkfolioException(MsgKOR.NOT_FOUND_RECORD_GROUP.message)
            }
            else -> return SuccessResponse.newBuilder().setIsSuccess(false).build()
        }

        return SuccessResponse.newBuilder().setIsSuccess(true).build()
    }
}
