package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.record.Record
import com.spectrum.workfolio.domain.entity.record.Record.Companion.generateRecordType
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.enums.RecordType
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.RecordRepository
import com.spectrum.workfolio.proto.record.ListRecordResponse
import com.spectrum.workfolio.proto.record.RecordCreateRequest
import com.spectrum.workfolio.proto.record.RecordResponse
import com.spectrum.workfolio.proto.record.RecordUpdateRequest
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

@Service
class RecordService(
    private val workerService: WorkerService,
    private val recordRepository: RecordRepository,
    private val recordGroupService: RecordGroupService,
    private val companyService: CompanyService,
) {

    private fun getRecordEntity(id: String): Record {
        return recordRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_RECORD.name) }
    }

    @Transactional(readOnly = true)
    fun getRecord(id: String): RecordResponse {
        val record = this.getRecordEntity(id)
        return RecordResponse.newBuilder().setRecord(record.toProto()).build()
    }

    @Transactional(readOnly = true)
    fun listMonthlyRecord(year: Int, month: Int, recordGroupIds: List<String>): ListRecordResponse {
        val startDate = LocalDateTime.of(year, month, 1, 0, 0, 0)
        val endDate = startDate.plusMonths(1)

        return getRecordByDateRange(startDate, endDate, recordGroupIds)
    }

    @Transactional(readOnly = true)
    fun listWeeklyRecord(weekStartDate: String, weekEndDate: String, recordGroupIds: List<String>): ListRecordResponse {
        val startDate = TimeUtil.dateStart(weekStartDate)
        val endDate = TimeUtil.dateEnd(weekEndDate)
        return getRecordByDateRange(startDate, endDate, recordGroupIds)
    }

    private fun getRecordByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        recordGroupIds: List<String>,
    ): ListRecordResponse {
        val list = recordRepository.findByDateRange(recordGroupIds, startDate, endDate)
        val sortedList =
            list.sortedWith(compareBy<Record> { it.startedAt.toLocalDate() }.thenByDescending { it.getDuration() })
        val listResponse = sortedList.map { it.toProto() }

        return ListRecordResponse.newBuilder().addAllRecords(listResponse).build()
    }

    @Transactional
    fun createRecord(workerId: String, request: RecordCreateRequest): RecordResponse {
        val worker = workerService.getWorker(workerId)
        val recordGroup = recordGroupService.getRecordGroup(request.recordGroupId)
        val startedAt = TimeUtil.ofEpochMilli(request.startedAt)
        val endedAt = TimeUtil.ofEpochMilli(request.endedAt)

        val company = if(request.hasCompanyId()) companyService.getCompany(request.companyId) else null

        // TODO TIME을 제외한 나머지 일자의 시간 조정 필요

        val record = Record(
            title = request.title,
            description = request.description,
            type = generateRecordType(startedAt, endedAt),
            startedAt = startedAt,
            endedAt = endedAt,
            recordGroup = recordGroup,
            company = company,
            worker = worker,
        )

        val createdRecord = recordRepository.save(record)

        return RecordResponse.newBuilder().setRecord(createdRecord.toProto()).build()
    }

    @Transactional
    fun updateRecord(request: RecordUpdateRequest): RecordResponse {
        val record = this.getRecordEntity(request.id)
        val company = if(request.hasCompanyId()) companyService.getCompany(request.companyId) else null
        val startedAt = TimeUtil.ofEpochMilli(request.startedAt)
        val endedAt = TimeUtil.ofEpochMilli(request.endedAt)

        record.changeInfo(
            title = request.title,
            description = request.description,
            type = generateRecordType(startedAt, endedAt),
            startedAt = startedAt,
            endedAt = endedAt,
            company = company,
        )

        val updatedRecord = recordRepository.save(record)

        return RecordResponse.newBuilder().setRecord(updatedRecord.toProto()).build()
    }

    @Transactional
    fun deleteRecord(workerId: String, recordId: String) {
        val worker = workerService.getWorker(workerId)
        val record = this.getRecordEntity(recordId)

        // TODO worker가 레코드 그룹에 편집자 이상 권한이 있을 경우에만 삭제 가능
//        if () {
//            throw WorkfolioException(MsgKOR.NOT_MATCH_RECORD_OWNER.name)
//        }

        recordRepository.delete(record)
    }
}
