package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.primary.Account
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface AccountRepository: JpaRepository<Account, String> {
    fun findByProviderId(providerId: String): Optional<Account>
}
