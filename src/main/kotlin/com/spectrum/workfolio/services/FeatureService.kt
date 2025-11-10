package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.entity.plan.Feature
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.FeatureRepository
import com.spectrum.workfolio.proto.feature.FeatureCreateRequest
import com.spectrum.workfolio.proto.feature.FeatureGetResponse
import com.spectrum.workfolio.proto.feature.FeatureListResponse
import com.spectrum.workfolio.proto.feature.FeatureUpdateRequest
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeatureService(
    private val featureRepository: FeatureRepository,
) {

    fun getFeatureById(id: String): Feature {
        return featureRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_FEATURE.message) }
    }

    @Transactional(readOnly = true)
    fun getFeatures(): FeatureListResponse {
        val features = featureRepository.findAllByOrderByDomainAscActionAsc()
        val featureProtos = features.map { it.toProto() }
        return FeatureListResponse.newBuilder().addAllFeatures(featureProtos).build()
    }

    @Transactional(readOnly = true)
    fun getFeature(id: String): FeatureGetResponse {
        val feature = getFeatureById(id)
        return FeatureGetResponse.newBuilder().setFeature(feature.toProto()).build()
    }

    @Transactional
    fun createFeature(request: FeatureCreateRequest) {
        val feature = Feature(
            name = request.name,
            domain = request.domain,
            action = request.action,
        )

        featureRepository.save(feature)
    }

    @Transactional
    fun updateFeature(request: FeatureUpdateRequest) {
        val feature = getFeatureById(request.id)

        feature.changeInfo(
            name = request.name,
            domain = request.domain,
            action = request.action,
        )

        featureRepository.save(feature)
    }

    @Transactional
    fun deleteFeature(id: String) {
        val feature = getFeatureById(id)
        featureRepository.delete(feature)
    }
}
