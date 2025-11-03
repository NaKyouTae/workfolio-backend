package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.dto.AttachmentCreateDto
import com.spectrum.workfolio.domain.dto.AttachmentUpdateDto
import com.spectrum.workfolio.domain.entity.record.Record
import com.spectrum.workfolio.domain.entity.record.Record.Companion.generateRecordType
import com.spectrum.workfolio.domain.entity.record.RecordAttachment
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.RecordRepository
import com.spectrum.workfolio.interfaces.AttachmentService
import com.spectrum.workfolio.proto.record.RecordCreateRequest
import com.spectrum.workfolio.proto.record.RecordResponse
import com.spectrum.workfolio.proto.record.RecordUpdateRequest
import com.spectrum.workfolio.utils.TimeUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordCommandService(
    private val workerService: WorkerService,
    private val recordRepository: RecordRepository,
    private val recordGroupService: RecordGroupService,
    private val recordQueryService: RecordQueryService,
    private val attachmentService: AttachmentService<RecordAttachment>,
) {

    @Transactional
    fun createRecord(workerId: String, request: RecordCreateRequest): RecordResponse {
        val worker = workerService.getWorker(workerId)
        val recordGroup = recordGroupService.getRecordGroup(request.recordGroupId)
        val startedAt = TimeUtil.ofEpochMilli(request.startedAt)
        val endedAt = TimeUtil.ofEpochMilli(request.endedAt)

        // TODO TIME을 제외한 나머지 일자의 시간 조정 필요

        val record = Record(
            title = request.title,
            description = request.description,
            type = generateRecordType(startedAt, endedAt),
            startedAt = startedAt,
            endedAt = endedAt,
            recordGroup = recordGroup,
            worker = worker,
        )

        val createdRecord = recordRepository.save(record)

        if (request.attachmentsList.isNotEmpty()) {
            request.attachmentsList.map {
                attachmentService.createAttachment(
                    AttachmentCreateDto(
                        targetId = createdRecord.id,
                        fileName = it.fileName,
                        fileData = it.fileData,
                        storagePath = "record/attachments/${createdRecord.id}",
                    ),
                )
            }
        }

        return RecordResponse.newBuilder().setRecord(createdRecord.toProto()).build()
    }

    @Transactional
    fun updateRecord(request: RecordUpdateRequest): RecordResponse {
        val record = recordQueryService.getRecordEntity(request.id)
        val startedAt = TimeUtil.ofEpochMilli(request.startedAt)
        val endedAt = TimeUtil.ofEpochMilli(request.endedAt)

        record.changeInfo(
            title = request.title,
            description = request.description,
            type = generateRecordType(startedAt, endedAt),
            startedAt = startedAt,
            endedAt = endedAt,
        )

        val updatedRecord = recordRepository.save(record)

        if (request.attachmentsList.isNotEmpty()) {
            request.attachmentsList.map {
                attachmentService.updateAttachment(
                    AttachmentUpdateDto(
                        id = updatedRecord.id,
                        fileName = it.fileName,
                        fileData = it.fileData,
                        storagePath = "record/attachments/${updatedRecord.id}",
                    ),
                )
            }
        }

        return RecordResponse.newBuilder().setRecord(updatedRecord.toProto()).build()
    }

    @Transactional
    fun deleteRecord(workerId: String, recordId: String) {
        val worker = workerService.getWorker(workerId)
        val record = recordQueryService.getRecordEntity(recordId)

        // TODO worker가 레코드 그룹에 편집자 이상 권한이 있을 경우에만 삭제 가능
//        if () {
//            throw WorkfolioException(MsgKOR.NOT_MATCH_RECORD_OWNER.name)
//        }

        recordRepository.delete(record)
    }
}
