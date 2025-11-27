package com.spectrum.workfolio.services

import com.spectrum.workfolio.config.service.oauth.KakaoApiProvider
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.enums.AccountType
import com.spectrum.workfolio.domain.enums.Gender
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.AccountRepository
import com.spectrum.workfolio.domain.repository.WorkerRepository
import com.spectrum.workfolio.proto.worker.WorkerGetResponse
import com.spectrum.workfolio.proto.worker.WorkerListResponse
import com.spectrum.workfolio.proto.worker.WorkerUpdateRequest
import com.spectrum.workfolio.utils.EnumUtils.convertProtoEnumSafe
import com.spectrum.workfolio.utils.TimeUtil
import com.spectrum.workfolio.utils.WorkfolioException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class WorkerService(
    private val workerRepository: WorkerRepository,
    private val accountRepository: AccountRepository,
    private val kakaoApiProvider: KakaoApiProvider,
) {

    private val logger = LoggerFactory.getLogger(WorkerService::class.java)

    @Transactional(readOnly = true)
    fun getWorker(id: String): Worker {
        return workerRepository.findById(id).orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_WORKER.message) }
    }

    @Transactional(readOnly = true)
    fun getWorkerResult(id: String): WorkerGetResponse {
        val worker = this.getWorker(id)
        return WorkerGetResponse.newBuilder().setWorker(worker.toProto()).build()
    }

    @Transactional(readOnly = true)
    fun getWorkersByNickName(workerId: String, nickName: String): WorkerListResponse {
        val workers = workerRepository.findWorkersExcludingIdByNickNameStartingWith(workerId, nickName)
        return WorkerListResponse.newBuilder().addAllWorkers(workers.map { it.toProto() }).build()
    }

    @Transactional(readOnly = true)
    fun checkNickNameAvailability(nickName: String): Boolean {
        return workerRepository.findByNickName(nickName) == null
    }

    @Transactional
    fun createWorker(worker: Worker): Worker {
        return workerRepository.save(worker)
    }

    @Transactional
    fun updateWorker(request: WorkerUpdateRequest): Worker {
        val worker = this.getWorker(request.id)

        worker.changeInfo(
            nickName = request.nickName,
            phone = request.phone ?: "",
            email = request.email ?: "",
            birthDate = TimeUtil.ofEpochMilliNullable(request.birthDate)?.toLocalDate(),
            gender = convertProtoEnumSafe<Gender>(request.gender),
        )

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

    /**
     * 회원 탈퇴 (카카오 연결 해제 포함)
     * 수동으로 관련 데이터를 순서대로 삭제하여 데이터 무결성 보장
     * @param workerId 탈퇴할 Worker ID
     * @return 탈퇴 성공 여부
     */
    @Transactional
    fun deleteWorker(workerId: String): Boolean {
        return try {
            logger.info("회원 탈퇴 시작: workerId={}", workerId)

            val worker = getWorker(workerId)

            // 카카오 계정인 경우 카카오 연결 해제
            val kakaoAccount = accountRepository.findByWorkerIdAndType(workerId, AccountType.KAKAO)
            if (kakaoAccount.isPresent) {
                logger.info("카카오 계정 연결 해제 시작: workerId={}", workerId)

                val unlinkSuccess = kakaoApiProvider.unlinkUser(kakaoAccount.get().providerId)
                if (!unlinkSuccess) {
                    logger.warn("카카오 연결 해제 실패, 계속 진행: workerId={}", workerId)
                } else {
                    logger.info("카카오 연결 해제 성공: workerId={}", workerId)
                }
            }

            // 3. Worker 삭제 (나머지 관련 데이터는 Cascade로 자동 삭제)
            workerRepository.delete(worker)
            logger.info("Worker 및 관련 데이터 삭제 완료: workerId={}", workerId)

            true
        } catch (e: Exception) {
            logger.error("회원 탈퇴 실패: workerId={}", workerId, e)
            throw WorkfolioException("회원 탈퇴 중 오류가 발생했습니다: ${e.message}")
        }
    }
}
