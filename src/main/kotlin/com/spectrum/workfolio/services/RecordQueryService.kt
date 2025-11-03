package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.record.Record
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.RecordRepository
import com.spectrum.workfolio.proto.record.ListRecordResponse
import com.spectrum.workfolio.proto.record.RecordResponse
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class RecordQueryService(
    private val recordRepository: RecordRepository,
) {

    fun getRecordEntity(id: String): Record {
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
}
