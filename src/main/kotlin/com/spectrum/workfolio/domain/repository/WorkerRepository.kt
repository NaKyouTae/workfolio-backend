package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.Worker
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WorkerRepository : JpaRepository<Worker, String> {
    fun findByNickName(nickName: String): Worker?
    fun findByNickNameStartingWith(nickName: String): List<Worker>
}
