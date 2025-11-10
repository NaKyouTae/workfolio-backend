package com.spectrum.workfolio.domain.entity.plan

import com.spectrum.workfolio.domain.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(
    name = "features",
    indexes = [
        Index(name = "idx_features_domain", columnList = "domain"),
        Index(name = "idx_features_domain_action", columnList = "domain, action"),
    ],
)
class Feature(
    name: String,
    domain: String,
    action: String,
) : BaseEntity("FE") {
    @Column(name = "name", length = 512, nullable = false)
    var name: String = name
        protected set

    @Column(name = "domain", length = 32, nullable = false)
    var domain: String = domain
        protected set

    @Column(name = "action", length = 512, nullable = false)
    var action: String = action
        protected set

    @OneToMany(mappedBy = "feature", fetch = FetchType.LAZY)
    private var mutablePlanFeatures: MutableList<PlanFeature> = mutableListOf()
    val planFeatures: List<PlanFeature> get() = mutablePlanFeatures.toList()

    fun changeInfo(
        name: String,
        domain: String,
        action: String,
    ) {
        this.name = name
        this.domain = domain
        this.action = action
    }
}
