package com.spectrum.workfolio.services.plan

import com.spectrum.workfolio.domain.entity.plan.Feature
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.repository.FeatureRepository
import com.spectrum.workfolio.proto.feature.FeatureCreateRequest
import com.spectrum.workfolio.proto.feature.FeatureUpdateRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Feature 명령 전용 서비스
 */
@Service
@Transactional
class FeatureCommandService(
    private val featureRepository: FeatureRepository,
) {

    fun createFeature(request: FeatureCreateRequest): Feature {
        val feature = Feature(
            name = request.name,
            domain = request.domain,
            action = request.action,
        )
        return featureRepository.save(feature)
    }

    fun updateFeature(request: FeatureUpdateRequest): Feature {
        val feature = featureRepository.findById(request.id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_FEATURE.message) }

        feature.changeInfo(
            name = request.name,
            domain = request.domain,
            action = request.action,
        )

        return featureRepository.save(feature)
    }

    fun deleteFeature(id: String) {
        val feature = featureRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_FEATURE.message) }

        featureRepository.delete(feature)
    }
}
