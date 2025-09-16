package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.model.MsgKOR
import com.spectrum.workfolio.domain.repository.WorkerRepository
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WorkerService(
    private val workerRepository: WorkerRepository,
) {
    @Transactional(readOnly = true)
    fun getWorker(id: String): Worker {
        return workerRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_WORKER.message) }
    }

    @Transactional(readOnly = true)
    fun getWorkersByNickName(nickName: String): List<Worker> {
        return workerRepository.findByNickNameStartingWith(nickName)
    }

    @Transactional
    fun createWorker(worker: Worker): Worker {
        return workerRepository.save(worker)
    }

    @Transactional
    fun changeWorkerNickName(workerId: String, nickName: String): Worker {
        val worker = getWorker(workerId)
        val existsNicknameWorker = workerRepository.findByNickName(nickName)

        if (worker.nickName == nickName) {
            throw WorkfolioException(MsgKOR.ALREADY_EXIST_WORKER_NICK_NAME.message)
        }
        if (existsNicknameWorker != null) {
            throw WorkfolioException(MsgKOR.ALREADY_EXIST_WORKER_NICK_NAME.message)
        }

        worker.changeNickName(nickName)

        return workerRepository.save(worker)
    }
}
