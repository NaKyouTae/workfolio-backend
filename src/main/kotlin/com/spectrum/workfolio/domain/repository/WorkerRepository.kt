package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.Worker
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface WorkerRepository : JpaRepository<Worker, String> {
    fun findByNickName(nickName: String): Worker?

    @Query("SELECT w FROM Worker w WHERE w.id != :workerId AND w.nickName LIKE :nickName%")
    fun findWorkersExcludingIdByNickNameStartingWith(
        @Param("workerId") workerId: String,
        @Param("nickName") nickName: String,
    ): List<Worker>
}
