package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.entity.payments.CreditHistory
import com.spectrum.workfolio.domain.enums.CreditTxType
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.CreditHistoryRepository
import com.spectrum.workfolio.domain.repository.WorkerRepository
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreditService(
    private val workerRepository: WorkerRepository,
    private val creditHistoryRepository: CreditHistoryRepository,
) {

    fun getWorkerById(workerId: String): Worker {
        return workerRepository.findById(workerId)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_WORKER.message) }
    }

    @Transactional(readOnly = true)
    fun getBalance(workerId: String): Int {
        val worker = getWorkerById(workerId)
        return worker.credit
    }

    @Transactional
    fun addCredits(
        workerId: String,
        amount: Int,
        txType: CreditTxType = CreditTxType.CHARGE,
        referenceType: String? = null,
        referenceId: String? = null,
        description: String? = null,
    ): CreditHistory {
        val worker = getWorkerById(workerId)
        val balanceBefore = worker.credit

        worker.addCredits(amount)
        workerRepository.save(worker)

        val history = CreditHistory(
            worker = worker,
            txType = txType,
            amount = amount,
            balanceBefore = balanceBefore,
            balanceAfter = worker.credit,
            referenceType = referenceType,
            referenceId = referenceId,
            description = description,
        )
        return creditHistoryRepository.save(history)
    }

    @Transactional
    fun useCredits(
        workerId: String,
        amount: Int,
        referenceType: String? = null,
        referenceId: String? = null,
        description: String? = null,
    ): CreditHistory {
        val worker = getWorkerById(workerId)

        if (!worker.hasEnoughCredits(amount)) {
            throw WorkfolioException("크레딧이 부족합니다.")
        }

        val balanceBefore = worker.credit
        worker.useCredits(amount)
        workerRepository.save(worker)

        val history = CreditHistory(
            worker = worker,
            txType = CreditTxType.USE,
            amount = -amount,
            balanceBefore = balanceBefore,
            balanceAfter = worker.credit,
            referenceType = referenceType,
            referenceId = referenceId,
            description = description,
        )
        return creditHistoryRepository.save(history)
    }

    @Transactional
    fun refundCredits(
        workerId: String,
        amount: Int,
        referenceType: String? = null,
        referenceId: String? = null,
        description: String? = null,
    ): CreditHistory {
        val worker = getWorkerById(workerId)

        if (!worker.hasEnoughCredits(amount)) {
            throw WorkfolioException("환불할 크레딧이 부족합니다.")
        }

        val balanceBefore = worker.credit
        worker.useCredits(amount)
        workerRepository.save(worker)

        val history = CreditHistory(
            worker = worker,
            txType = CreditTxType.REFUND,
            amount = -amount,
            balanceBefore = balanceBefore,
            balanceAfter = worker.credit,
            referenceType = referenceType,
            referenceId = referenceId,
            description = description,
        )
        return creditHistoryRepository.save(history)
    }

    @Transactional(readOnly = true)
    fun getHistory(workerId: String, pageable: Pageable): Page<CreditHistory> {
        return creditHistoryRepository.findByWorkerIdOrderByCreatedAtDesc(workerId, pageable)
    }

    @Transactional(readOnly = true)
    fun getHistoryByType(workerId: String, txType: CreditTxType, pageable: Pageable): Page<CreditHistory> {
        return creditHistoryRepository.findByWorkerIdAndTxTypeOrderByCreatedAtDesc(workerId, txType, pageable)
    }

    @Transactional(readOnly = true)
    fun getAllHistory(pageable: Pageable): Page<CreditHistory> {
        return creditHistoryRepository.findAllByOrderByCreatedAtDesc(pageable)
    }

    @Transactional(readOnly = true)
    fun getAllHistoryByType(txType: CreditTxType, pageable: Pageable): Page<CreditHistory> {
        return creditHistoryRepository.findByTxTypeOrderByCreatedAtDesc(txType, pageable)
    }

    @Transactional(readOnly = true)
    fun getHistoryById(id: String): CreditHistory {
        return creditHistoryRepository.findById(id)
            .orElseThrow { WorkfolioException("크레딧 내역을 찾을 수 없습니다.") }
    }
}
