package com.spectrum.workfolio.services

import com.spectrum.workfolio.config.service.oauth.OAuthUserInfo
import com.spectrum.workfolio.config.service.oauth.UserRegistrationException
import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.entity.primary.Account
import com.spectrum.workfolio.domain.entity.record.RecordGroup
import com.spectrum.workfolio.domain.model.AccountType
import com.spectrum.workfolio.proto.record.CreateRecordRequest
import com.spectrum.workfolio.proto.record_group.CreateRecordGroupRequest
import com.spectrum.workfolio.utils.StringUtil
import com.spectrum.workfolio.utils.TimeUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 사용자 등록 관련 비즈니스 로직을 담당하는 서비스
 */
@Service
class UserRegistrationService(
    private val workerService: WorkerService,
    private val accountService: AccountService,
    private val recordGroupService: RecordGroupService,
    private val recordService: RecordService,
) {
    
    private val logger = LoggerFactory.getLogger(UserRegistrationService::class.java)
    
    /**
     * OAuth 사용자 정보를 기반으로 새 사용자를 등록합니다.
     * Worker, Account, 기본 RecordGroup, 기본 Record를 생성합니다.
     */
    @Transactional
    fun registerNewUser(
        oauthUserInfo: OAuthUserInfo,
        accountType: AccountType
    ): Account {
        logger.info("새 사용자 등록 시작: providerId={}, name={}", oauthUserInfo.providerId, oauthUserInfo.name)
        
        try {
            // 1. Worker 생성
            val worker = createWorker(oauthUserInfo)
            
            // 2. 기본 RecordGroup 생성
            val recordGroup = createDefaultRecordGroup(worker.id)
            
            // 3. 기본 Record 생성
            createDefaultRecord(worker.id, recordGroup.id)
            
            // 4. Account 생성
            val account = createAccount(oauthUserInfo, accountType, worker)
            
            logger.info("새 사용자 등록 완료: workerId={}, accountId={}", worker.id, account.id)
            return account
            
        } catch (e: Exception) {
            logger.error("사용자 등록 중 오류 발생: providerId={}", oauthUserInfo.providerId, e)
            throw UserRegistrationException(oauthUserInfo.providerId, e)
        }
    }
    
    private fun createWorker(oauthUserInfo: OAuthUserInfo): Worker {
        val worker = Worker(name = oauthUserInfo.name, nickName = StringUtil.generateRandomString(8))
        return workerService.createWorker(worker)
    }
    
    private fun createDefaultRecordGroup(workerId: String): RecordGroup {
        val recordGroupRequest = CreateRecordGroupRequest.newBuilder()
            .setColor("red")
            .setTitle("기본 그룹")
            .setPriority(0)
            .build()
        
        return recordGroupService.createRecordGroup(workerId, recordGroupRequest)
    }
    
    private fun createDefaultRecord(workerId: String, recordGroupId: String) {
        val now = TimeUtil.nowToLong()
        val recordRequest = CreateRecordRequest.newBuilder()
            .setTitle("워크폴리오 회원 가입")
            .setMemo("회원가입을 축하드립니다.")
            .setStartedAt(now)
            .setEndedAt(now)
            .setRecordGroupId(recordGroupId)
            .build()
        
        recordService.create(workerId, recordRequest)
    }
    
    private fun createAccount(
        oauthUserInfo: OAuthUserInfo,
        accountType: AccountType,
        worker: Worker
    ): Account {
        val account = Account(
            type = accountType,
            email = oauthUserInfo.email,
            providerId = oauthUserInfo.providerId,
            worker = worker
        )
        
        accountService.createAccount(account)
        return account
    }
}
