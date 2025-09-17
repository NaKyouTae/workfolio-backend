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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

@Service
class RecordService(
    private val workerService: WorkerService,
    private val recordRepository: RecordRepository,
    private val recordGroupService: RecordGroupService,
) {

    @Transactional(readOnly = true)
    fun listMonthlyRecord(year: Int, month: Int, recordGroupIds: List<String>): ListRecordResponse {
        val startDate = LocalDateTime.of(year, month, 1, 0, 0, 0)
        val endDate = startDate.plusMonths(1)

        return getRecordByDateRange(startDate, endDate, recordGroupIds)
    }

    @Transactional(readOnly = true)
    fun listWeeklyRecord(year: Int, month: Int, week: Int, recordGroupIds: List<String>): ListRecordResponse {
        val (weekStart, weekEnd) = calculateWeekRange(year, month, week)

        // 주가 해당 월 범위를 벗어나는지 확인
        val monthStart = LocalDate.of(year, month, 1)
        val monthEnd = monthStart.with(TemporalAdjusters.lastDayOfMonth())

        if (weekStart.toLocalDate().isAfter(monthEnd) || weekEnd.toLocalDate().isBefore(monthStart)) {
            return ListRecordResponse.newBuilder().build()
        }

        return getRecordByDateRange(weekStart, weekEnd, recordGroupIds)
    }

    private fun getRecordByDateRange(startDate: LocalDateTime, endDate: LocalDateTime, recordGroupIds: List<String>): ListRecordResponse {
        val list = recordRepository.findByDateRange(recordGroupIds, startDate, endDate)
        val sortedList = list.sortedWith(compareBy<Record> { it.startedAt.toLocalDate() }.thenByDescending { it.getDuration() })
        val listResponse = sortedList.map { it.toRecordProto() }

        return ListRecordResponse.newBuilder().addAllRecords(listResponse).build()
    }

    @Transactional
    fun create(workerId: String, params: CreateRecordRequest): Record {
        val worker = workerService.getWorker(workerId)
        val recordGroup = recordGroupService.getRecordGroup(params.recordGroupId)
        val startedAt = TimeUtil.ofEpochMilli(params.startedAt)
        val endedAt = TimeUtil.ofEpochMilli(params.endedAt)

        // TODO TIME을 제외한 나머지 일자의 시간 조정 필요

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

    private fun calculateWeekRange(year: Int, month: Int, week: Int): Pair<LocalDateTime, LocalDateTime> {
        val monthStart = LocalDate.of(year, month, 1)

        // 해당 월의 첫 번째 월요일 찾기
        val firstMonday = monthStart.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY))

        // 요청된 주차의 시작일 계산
        val weekStart = firstMonday.plusWeeks((week - 1).toLong())
        val weekEnd = weekStart.plusDays(6)

        return Pair(
            weekStart.atStartOfDay(),
            weekEnd.atTime(23, 59, 59)
        )
    }
}
