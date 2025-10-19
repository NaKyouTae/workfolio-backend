package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.SystemConfig
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.enums.SystemConfigType
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.SystemConfigRepository
import com.spectrum.workfolio.proto.worker.SystemConfigCreateRequest
import com.spectrum.workfolio.proto.worker.SystemConfigGetResponse
import com.spectrum.workfolio.proto.worker.SystemConfigUpdateRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SystemConfigService(
    private val workerService: WorkerService,
    private val systemConfigRepository: SystemConfigRepository,
) {

    fun getSystemConfigById(id: String): SystemConfig {
        return systemConfigRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_SYSTEM_CONFIG.message) }
    }

    fun getSystemConfig(type: SystemConfigType, workerId: String): SystemConfig {
        return systemConfigRepository.findByTypeAndWorkerId(type, workerId)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_SYSTEM_CONFIG.message) }
    }

    @Transactional(readOnly = true)
    fun getSystemConfigResult(type: String, workerId: String): SystemConfigGetResponse {
        val systemConfigResult = getSystemConfig(SystemConfigType.valueOf(type.uppercase()), workerId).toProto()
        return SystemConfigGetResponse.newBuilder().setSystemConfig(systemConfigResult).build()
    }

    @Transactional
    fun createSystemConfig(request: SystemConfigCreateRequest) {
        val worker = workerService.getWorker(request.workerId)
        val systemConfig = SystemConfig(
            type = SystemConfigType.valueOf(request.type.name),
            value = request.value.uppercase(),
            worker = worker,
        )

        systemConfigRepository.save(systemConfig)
    }

    @Transactional
    fun updateSystemConfig(request: SystemConfigUpdateRequest) {
        val systemConfig = getSystemConfigById(request.id)

        systemConfig.changeInfo(systemConfig.type, request.value)

        systemConfigRepository.save(systemConfig)
    }
}
