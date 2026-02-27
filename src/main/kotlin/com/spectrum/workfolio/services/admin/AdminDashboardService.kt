package com.spectrum.workfolio.services.admin

import com.spectrum.workfolio.domain.enums.CreditTxType
import com.spectrum.workfolio.domain.repository.AttachmentRepository
import com.spectrum.workfolio.domain.repository.CreditHistoryRepository
import com.spectrum.workfolio.domain.repository.PaymentRepository
import com.spectrum.workfolio.domain.repository.RecordGroupRepository
import com.spectrum.workfolio.domain.repository.RecordRepository
import com.spectrum.workfolio.domain.repository.ResumeRepository
import com.spectrum.workfolio.domain.repository.TurnOverRepository
import org.springframework.stereotype.Service

data class AdminDashboardStatsResponse(
    val totalRecordGroups: Long,
    val totalRecords: Long,
    val totalTurnOvers: Long,
    val totalCareers: Long,
    val totalPaymentAmount: Long,
    val totalCreditUsedAmount: Long,
    val totalAttachments: Long,
)

@Service
class AdminDashboardService(
    private val recordGroupRepository: RecordGroupRepository,
    private val recordRepository: RecordRepository,
    private val turnOverRepository: TurnOverRepository,
    private val resumeRepository: ResumeRepository,
    private val paymentRepository: PaymentRepository,
    private val creditHistoryRepository: CreditHistoryRepository,
    private val attachmentRepository: AttachmentRepository,
) {
    fun getStats(): AdminDashboardStatsResponse {
        val totalPaymentAmount = paymentRepository.sumAmountByStatuses(listOf("COMPLETED", "REFUNDED")).toLong()
        val totalCreditUsedAmount = creditHistoryRepository.sumAmountByTxTypes(listOf(CreditTxType.USE, CreditTxType.ADMIN_DEDUCT))

        return AdminDashboardStatsResponse(
            totalRecordGroups = recordGroupRepository.count(),
            totalRecords = recordRepository.count(),
            totalTurnOvers = turnOverRepository.count(),
            totalCareers = resumeRepository.count(),
            totalPaymentAmount = totalPaymentAmount,
            totalCreditUsedAmount = totalCreditUsedAmount,
            totalAttachments = attachmentRepository.count(),
        )
    }
}
