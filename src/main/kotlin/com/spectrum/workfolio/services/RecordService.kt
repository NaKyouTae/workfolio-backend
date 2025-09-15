package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.record.Record
import com.spectrum.workfolio.domain.entity.record.Record.Companion.generateRecordType
import com.spectrum.workfolio.domain.extensions.toRecordProto
import com.spectrum.workfolio.domain.repository.RecordRepository
import com.spectrum.workfolio.proto.record.CreateRecordRequest
import com.spectrum.workfolio.proto.record.ListRecordResponse
import com.spectrum.workfolio.utils.TimeUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RecordService(
    private val workerService: WorkerService,
    private val recordRepository: RecordRepository,
    private val recordGroupService: RecordGroupService,
) {

    @Transactional(readOnly = true)
    fun listProto(workerId: String): ListRecordResponse {
        val list = listByWorker(workerId)
        val sortedList = list.sortedWith(compareBy<Record> { it.startedAt.toLocalDate() }.thenByDescending { it.getDuration() })
        val listResponse = sortedList.map { it.toRecordProto() }
        return ListRecordResponse.newBuilder().addAllRecords(listResponse).build()
    }

    private fun listByWorker(workerId: String): List<Record> {
        val worker =
            workerService.getWorker(workerId)
        return recordRepository.findAllByWorker(worker)
    }

    @Transactional
    fun create(workerId: String, params: CreateRecordRequest): Record {
        val worker = workerService.getWorker(workerId)
        val recordGroup = recordGroupService.getRecordGroup(params.recordGroupId)
        val startedAt = TimeUtil.ofEpochMilli(params.startedAt)
        val endedAt = TimeUtil.ofEpochMilli(params.endedAt)

        val record = Record(
            title = params.title,
            description = params.memo,
            type = generateRecordType(startedAt, endedAt),
            startedAt = startedAt,
            endedAt = endedAt,
            recordGroup,
            worker,
        )

        return recordRepository.save(record)
    }
}
