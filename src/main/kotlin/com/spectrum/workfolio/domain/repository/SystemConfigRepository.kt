package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.SystemConfig
import com.spectrum.workfolio.domain.enums.SystemConfigType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface SystemConfigRepository : JpaRepository<SystemConfig, String> {
    fun findByTypeAndWorkerId(type: SystemConfigType, workerId: String): Optional<SystemConfig>
}
