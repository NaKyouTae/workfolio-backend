package com.spectrum.workfolio.services

import com.spectrum.workfolio.config.service.oauth.OAuthUserInfo
import com.spectrum.workfolio.config.service.oauth.UserRegistrationException
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.entity.primary.Account
import com.spectrum.workfolio.domain.enums.AccountType
import com.spectrum.workfolio.proto.common.RecordGroup
import com.spectrum.workfolio.proto.common.SystemConfig
import com.spectrum.workfolio.proto.record.RecordCreateRequest
import com.spectrum.workfolio.proto.record_group.CreateRecordGroupRequest
import com.spectrum.workfolio.proto.record_group.RecordGroupResponse
import com.spectrum.workfolio.proto.worker.SystemConfigCreateRequest
import com.spectrum.workfolio.utils.StringUtil
import com.spectrum.workfolio.utils.TimeUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 사용자 등록 관련 비즈니스 로직을 담당하는 서비스
 */
@Service
class UserRegistrationService(
    private val recordService: RecordService,
    private val workerService: WorkerService,
    private val accountService: AccountService,
    private val recordGroupService: RecordGroupService,
    private val systemConfigService: SystemConfigService,
) {

    private val logger = LoggerFactory.getLogger(UserRegistrationService::class.java)

    /**
     * OAuth 사용자 정보를 기반으로 새 사용자를 등록합니다.
     * Worker, Account, 기본 RecordGroup, 기본 Record를 생성합니다.
     */
    @Transactional
    fun registerNewUser(
        oauthUserInfo: OAuthUserInfo,
        accountType: AccountType,
    ): Account {
        logger.info("새 사용자 등록 시작: providerId={}, name={}", oauthUserInfo.providerId, oauthUserInfo.nickName)

        try {
            // 1. Worker 생성
            val worker = createWorker(oauthUserInfo)

            // 2. 기본 RecordGroup 생성
            val recordGroup = createDefaultRecordGroup(worker.id)

            // 3. 기본 Record 생성
            createDefaultRecord(worker.id, recordGroup.recordGroup.id)

            // 4. Account 생성
            val account = createAccount(oauthUserInfo, accountType, worker)

            // 5. 설정 생성
            createSystemConfig(worker)

            logger.info("새 사용자 등록 완료: workerId={}, accountId={}", worker.id, account.id)
            return account
        } catch (e: Exception) {
            logger.error("사용자 등록 중 오류 발생: providerId={}", oauthUserInfo.providerId, e)
            throw UserRegistrationException(oauthUserInfo.providerId, e)
        }
    }

    private fun createWorker(oauthUserInfo: OAuthUserInfo): Worker {
        val worker = Worker(
            nickName = oauthUserInfo.nickName,
            phone = oauthUserInfo.phoneNumber,
            email = oauthUserInfo.email,
            birthDate = oauthUserInfo.birthDate,
            gender = oauthUserInfo.gender,
        )
        return workerService.createWorker(worker)
    }

    private fun createDefaultRecordGroup(workerId: String): RecordGroupResponse {
        val recordGroupRequest = CreateRecordGroupRequest.newBuilder()
            .setColor("red")
            .setTitle("개인 기록장")
            .setType(RecordGroup.RecordGroupType.PRIVATE)
            .setPriority(0)
            .build()

        return recordGroupService.createRecordGroup(workerId, true, recordGroupRequest)
    }

    private fun createDefaultRecord(workerId: String, recordGroupId: String) {
        val now = LocalDateTime.now()
        val startedAt = TimeUtil.toEpochMilli(TimeUtil.dateStart(now))
        val endedAt = TimeUtil.toEpochMilli(TimeUtil.dateEnd(now))
        val recordRequest = RecordCreateRequest.newBuilder()
            .setTitle("워크폴리오 회원 가입")
            .setDescription("회원가입을 축하드립니다.")
            .setStartedAt(startedAt)
            .setEndedAt(endedAt)
            .setRecordGroupId(recordGroupId)
            .build()

        recordService.createRecord(workerId, recordRequest)
    }

    private fun createAccount(
        oauthUserInfo: OAuthUserInfo,
        accountType: AccountType,
        worker: Worker,
    ): Account {
        val account = Account(
            type = accountType,
            email = oauthUserInfo.email,
            providerId = oauthUserInfo.providerId,
            worker = worker,
        )

        accountService.createAccount(account)
        return account
    }

    private fun createSystemConfig(worker: Worker) {
        // 기본 레코드 타입 설정 생성
        val request = SystemConfigCreateRequest.newBuilder()
            .setType(SystemConfig.SystemConfigType.DEFAULT_RECORD_TYPE)
            .setValue("MONTHLY")
            .setWorkerId(worker.id)
            .build()

        systemConfigService.createSystemConfig(request)
    }
}
