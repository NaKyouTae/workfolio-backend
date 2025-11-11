package com.spectrum.workfolio.config

import com.spectrum.workfolio.domain.entity.Staff
import com.spectrum.workfolio.domain.repository.StaffRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@Configuration
class DataInitializer {

    private val logger = LoggerFactory.getLogger(DataInitializer::class.java)

    @Bean
    fun initDefaultStaff(
        staffRepository: StaffRepository,
        passwordEncoder: BCryptPasswordEncoder,
    ): CommandLineRunner {
        return CommandLineRunner {
            // 기본 관리자 계정이 이미 존재하는지 확인
            val existingStaff = staffRepository.findByUsername("spectrum")
            if (existingStaff == null) {
                val defaultStaff = Staff(
                    username = "spectrum",
                    password = passwordEncoder.encode("Skrbxo123!@#"),
                    name = "워크폴리오 관리자",
                    email = "admin@spectrum.com",
                    phone = "010-9109-2682",
                    isActive = true,
                )
                staffRepository.save(defaultStaff)
                logger.info("✅ 기본 관리자 계정이 생성되었습니다: username=spectrum")
            } else {
                logger.info("ℹ️ 기본 관리자 계정이 이미 존재합니다: username=spectrum")
            }
        }
    }
}

