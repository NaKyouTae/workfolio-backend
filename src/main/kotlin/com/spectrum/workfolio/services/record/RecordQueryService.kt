package com.spectrum.workfolio.services.record

import com.spectrum.workfolio.domain.entity.record.Record
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.RecordRepository
import com.spectrum.workfolio.proto.record.ListRecordResponse
import com.spectrum.workfolio.proto.record.RecordResponse
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class RecordQueryService(
    private val recordRepository: RecordRepository,
) {

    fun getRecordEntity(id: String): Record {
        return recordRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_RECORD.name) }
    }

    fun getRecord(id: String): RecordResponse {
        val record = this.getRecordEntity(id)
        return RecordResponse.newBuilder().setRecord(record.toProto()).build()
    }

    // 내 이력서 검색
    fun searchMyRecords(recordGroupIds: List<String>, keyword: String): ListRecordResponse {
        val list = recordRepository.searchByFullText(recordGroupIds, keyword)
        return convertListRecordResponse(list)
    }

    // 관련성 높은 순으로 검색
    fun searchMyRecordsRanked(workerId: String, keyword: String): ListRecordResponse {
        val list = recordRepository.searchByFullTextRanked(workerId, keyword)
        return convertListRecordResponse(list)
    }

    // 공개 이력서 검색 (전체 검색)
    fun searchPublicRecords(keyword: String, limit: Int = 20): ListRecordResponse {
        val list = recordRepository.searchPublicRecords(keyword, limit)
        return convertListRecordResponse(list)
    }

    fun listMonthlyRecord(year: Int, month: Int, recordGroupIds: List<String>): ListRecordResponse {
        val startDate = LocalDateTime.of(year, month, 1, 0, 0, 0)
        val endDate = startDate.plusMonths(1)

        return getRecordByDateRange(startDate, endDate, recordGroupIds)
    }

    fun listWeeklyRecord(weekStartDate: String, weekEndDate: String, recordGroupIds: List<String>): ListRecordResponse {
        val startDate = TimeUtil.dateStart(weekStartDate)
        val endDate = TimeUtil.dateEnd(weekEndDate)
        return getRecordByDateRange(startDate, endDate, recordGroupIds)
    }

    fun listAllRecords(recordGroupIds: List<String>, pageable: Pageable): Page<Record> {
        return recordRepository.findAllByRecordGroupIds(recordGroupIds, pageable)
    }

    private fun getRecordByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        recordGroupIds: List<String>,
    ): ListRecordResponse {
        val list = recordRepository.findByDateRange(recordGroupIds, startDate, endDate)
        return convertListRecordResponse(list)
    }

    private fun convertListRecordResponse(list: List<Record>): ListRecordResponse {
        val sortedList =
            list.sortedWith(compareBy<Record> { it.startedAt.toLocalDate() }.thenByDescending { it.getDuration() })
        val listResponse = sortedList.map { it.toProto() }

        return ListRecordResponse.newBuilder().addAllRecords(listResponse).build()
    }
}
