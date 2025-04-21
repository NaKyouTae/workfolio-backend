package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.primary.Account
import com.spectrum.workfolio.domain.repository.AccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Optional

@Service
class AccountService(
    private val accountRepository: AccountRepository,
) {

    @Transactional(readOnly = true)
    fun getAccountByProviderId(providerId: String): Optional<Account> {
        return accountRepository.findByProviderId(providerId)
    }

    @Transactional
    fun createAccount(account: Account) {
        accountRepository.save(account)
    }
}
