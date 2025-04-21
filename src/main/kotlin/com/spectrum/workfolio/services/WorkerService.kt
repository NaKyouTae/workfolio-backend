package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.repository.WorkerRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Service
class WorkerService(
    private val workerRepository: WorkerRepository,
) {
    @Transactional(readOnly = true)
    fun getWorker(id: String): Optional<Worker> {
        return workerRepository.findById(id)
    }

    @Transactional
    fun createWorker(worker: Worker): Worker {
        return workerRepository.save(worker)
    }
}
