package com.spectrum.workfolio.controllers

import com.spectrum.workfolio.config.annotation.AuthenticatedUser
import com.spectrum.workfolio.domain.extensions.toWorkerProto
import com.spectrum.workfolio.proto.worker.WorkerGetResponse
import com.spectrum.workfolio.proto.worker.WorkerListResponse
import com.spectrum.workfolio.proto.worker.WorkerUpdateNickNameResponse
import com.spectrum.workfolio.services.WorkerService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/workers")
class WorkerController(
    private val workerService: WorkerService,
) {

    @GetMapping("/me")
    fun getWorker(
        @AuthenticatedUser workerId: String,
    ): WorkerGetResponse {
        val worker = workerService.getWorker(workerId)
        return WorkerGetResponse.newBuilder().setWorker(worker.toWorkerProto()).build()
    }

    @GetMapping("/{nickname}")
    fun getWorkerByNickName(
        @PathVariable nickname: String
    ): WorkerListResponse {
        val workers = workerService.getWorkersByNickName(nickname)
        return WorkerListResponse.newBuilder().addAllWorkers(workers.map { it.toWorkerProto() }).build()
    }

    @PutMapping("/{nickname}")
    fun changeWorkerNickName(
        @AuthenticatedUser workerId: String,
        @PathVariable nickname: String,
    ): WorkerUpdateNickNameResponse {
        workerService.changeWorkerNickName(workerId, nickname)
        return WorkerUpdateNickNameResponse.newBuilder().setIsSuccess(true).build()
    }

    @DeleteMapping("/me")
    fun deleteWorker(
        @AuthenticatedUser workerId: String,
    ): ResponseEntity<Map<String, Any>> {
        val success = workerService.deleteWorker(workerId)
        
        return if (success) {
            ResponseEntity.ok(mapOf(
                "success" to true,
                "message" to "회원 탈퇴가 완료되었습니다."
            ))
        } else {
            ResponseEntity.badRequest().body(mapOf(
                "success" to false,
                "message" to "회원 탈퇴 중 오류가 발생했습니다."
            ))
        }
    }
}
