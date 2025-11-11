package com.spectrum.workfolio.services.plan

import com.spectrum.workfolio.domain.entity.plan.Feature
import com.spectrum.workfolio.domain.enums.MsgKOR
import com.spectrum.workfolio.domain.extensions.toProto
import com.spectrum.workfolio.domain.repository.FeatureRepository
import com.spectrum.workfolio.proto.feature.FeatureGetResponse
import com.spectrum.workfolio.proto.feature.FeatureListResponse
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Feature 조회 전용 서비스
 */
@Service
@Transactional(readOnly = true)
class FeatureQueryService(
    private val featureRepository: FeatureRepository,
) {

    fun getFeature(id: String): Feature {
        return featureRepository.findById(id)
            .orElseThrow { WorkfolioException(MsgKOR.NOT_FOUND_FEATURE.message) }
    }

    fun getFeatureResult(id: String): FeatureGetResponse {
        val feature = getFeature(id)
        return FeatureGetResponse.newBuilder()
            .setFeature(feature.toProto())
            .build()
    }

    fun listFeatures(): List<Feature> {
        return featureRepository.findAllByOrderByDomainAscActionAsc()
    }

    fun listFeaturesByDomain(domain: String): List<Feature> {
        return featureRepository.findByDomain(domain)
    }

    fun listFeaturesResult(): FeatureListResponse {
        val features = listFeatures()
        return FeatureListResponse.newBuilder()
            .addAllFeatures(features.map { it.toProto() })
            .build()
    }

    fun listFeaturesByDomainResult(domain: String): FeatureListResponse {
        val features = listFeaturesByDomain(domain)
        return FeatureListResponse.newBuilder()
            .addAllFeatures(features.map { it.toProto() })
            .build()
    }
}

