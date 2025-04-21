package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.Worker
import com.spectrum.workfolio.domain.entity.record.Record
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RecordRepository: JpaRepository<Record, String> {
    fun findAllByWorker(worker: Worker): List<Record>
}
