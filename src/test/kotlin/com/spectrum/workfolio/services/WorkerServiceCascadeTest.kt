package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.entity.primary.Account
import com.spectrum.workfolio.domain.model.AccountType
import com.spectrum.workfolio.domain.repository.AccountRepository
import com.spectrum.workfolio.domain.repository.WorkerRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

/**
 * Worker 삭제 시 Cascade 동작 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WorkerServiceCascadeTest {

    @Autowired
    private lateinit var workerService: WorkerService

    @Autowired
    private lateinit var workerRepository: WorkerRepository

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Test
    fun `Worker 삭제 시 관련 데이터가 모두 삭제되는지 확인`() {
        // Given
        val worker = Worker("테스트 사용자", "testuser")
        val savedWorker = workerRepository.save(worker)

        val account = Account(
            type = AccountType.KAKAO,
            providerId = "test123",
            worker = savedWorker,
        )
        accountRepository.save(account)

        // When
        workerService.deleteWorker(savedWorker.id)

        // Then
        // Worker가 삭제되었는지 확인
        assert(!workerRepository.existsById(savedWorker.id))

        // 관련 Account도 삭제되었는지 확인
        assert(accountRepository.findByWorkerId(savedWorker.id).isEmpty())
    }
}
