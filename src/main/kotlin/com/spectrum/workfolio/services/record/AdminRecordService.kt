package com.spectrum.workfolio.services.record

import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.RecordGroupRepository
import com.spectrum.workfolio.proto.record.AdminRecordListResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminRecordService(
    private val recordGroupRepository: RecordGroupRepository,
    private val workerRecordGroupService: WorkerRecordGroupService,
    private val recordQueryService: RecordQueryService,
) {

    @Transactional(readOnly = true)
    fun getRecordsByWorkerId(workerId: String, page: Int, size: Int): AdminRecordListResponse {
        val safePage = page.coerceAtLeast(0)
        val safeSize = size.coerceIn(1, 200)

        val ownedGroups = recordGroupRepository.findByWorkerIdOrderByPriorityDesc(workerId)
        val sharedGroups = workerRecordGroupService.listWorkerRecordGroupByWorkerId(workerId)
        val groupIds = (ownedGroups.map { it.id } + sharedGroups.map { it.recordGroup.id }).distinct()

        val pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "startedAt"))
        val recordsPage = if (groupIds.isEmpty()) {
            Page.empty(pageable)
        } else {
            recordQueryService.listAllRecords(groupIds, pageable)
        }

        val recordProtos = recordsPage.content.map { it.toProto() }

        return AdminRecordListResponse.newBuilder()
            .addAllRecords(recordProtos)
            .setTotalElements(recordsPage.totalElements.toInt())
            .setTotalPages(recordsPage.totalPages)
            .setCurrentPage(safePage)
            .build()
    }
}
