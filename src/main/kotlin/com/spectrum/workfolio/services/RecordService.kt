package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.record.Record
import com.spectrum.workfolio.domain.extensions.toRecordProto
import com.spectrum.workfolio.domain.model.RecordType
import com.spectrum.workfolio.domain.repository.RecordRepository
import com.spectrum.workfolio.proto.CreateRecordRequest
import com.spectrum.workfolio.proto.ListRecordResponse
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.LocalDateTime

@Service
class RecordService(
    private val workerService: WorkerService,
    private val recordRepository: RecordRepository,
    private val recordGroupService: RecordGroupService,
) {

    @Transactional(readOnly = true)
    fun listProto(workerId: String): ListRecordResponse {
        val list = list(workerId)
        val listResponse = list.map { it.toRecordProto() }
        return ListRecordResponse.newBuilder().addAllRecords(listResponse).build()
    }

    private fun list(workerId: String): List<Record> {
        val worker =
            workerService.getWorker(workerId).orElseThrow { throw WorkfolioException("Worker $workerId not found") }
        return recordRepository.findAllByWorker(worker)
    }

    @Transactional
    fun create(workerId: String, params: CreateRecordRequest): Record {
        val worker = workerService.getWorker(workerId)
            .orElseThrow { throw WorkfolioException("Worker $workerId does not exist") }
        val recordGroup = recordGroupService.getRecordGroup(params.recordGroupId)
            .orElseThrow { throw WorkfolioException("RecordGroup ${params.recordGroupId} does not exist") }
        val startedAt = TimeUtil.ofEpochMilli(params.startedAt)
        val endedAt = TimeUtil.ofEpochMilli(params.endedAt)
        val type = generateRecordType(startedAt, endedAt)
        val record = Record(
            title = params.title,
            description = params.memo,
            type = type,
            startedAt = startedAt,
            endedAt = endedAt,
            recordGroup,
            worker,
        )

        return recordRepository.save(record)
    }

    fun generateRecordType(startedAt: LocalDateTime, endedAt: LocalDateTime): RecordType {
        val duration = Duration.between(startedAt, endedAt)

        return when {
            duration.toDays() >= 1 -> RecordType.MULTI_DAY
            duration.toHours() < 24 -> RecordType.TIME
            else -> RecordType.DAY
        }
    }
}
