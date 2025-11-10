package com.spectrum.workfolio.domain.repository

import com.spectrum.workfolio.domain.entity.plan.Feature
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FeatureRepository : JpaRepository<Feature, String> {
    fun findByDomain(domain: String): List<Feature>
    fun findAllByOrderByDomainAscActionAsc(): List<Feature>
}
