package com.spectrum.workfolio.domain.entity.uitemplate

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.enums.UITemplateType
import jakarta.persistence.*

@Entity
@Table(
    name = "ui_templates",
    indexes = [
        Index(name = "idx_ui_templates_type_active_order", columnList = "type, is_active, display_order"),
    ],
)
class UITemplate(
    name: String,
    description: String? = null,
    type: UITemplateType,
    label: String? = null,
    price: Int,
    durationDays: Int,
    urlPath: String? = null,
    isActive: Boolean = true,
    isPopular: Boolean = false,
    displayOrder: Int = 0,
) : BaseEntity("UT") {

    @Column(name = "name", length = 256, nullable = false)
    var name: String = name
        protected set

    @Column(name = "description", columnDefinition = "TEXT", nullable = true)
    var description: String? = description
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 32, nullable = false)
    var type: UITemplateType = type
        protected set

    @Column(name = "label", length = 64, nullable = true)
    var label: String? = label
        protected set

    @Column(name = "price", nullable = false)
    var price: Int = price
        protected set

    @Column(name = "duration_days", nullable = false)
    var durationDays: Int = durationDays
        protected set

    @Column(name = "url_path", length = 128, nullable = true)
    var urlPath: String? = urlPath
        protected set

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = isActive
        protected set

    @Column(name = "is_popular", nullable = false)
    var isPopular: Boolean = isPopular
        protected set

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = displayOrder
        protected set

    @OneToMany(mappedBy = "uiTemplate", fetch = FetchType.LAZY)
    var images: MutableList<UITemplateImage> = mutableListOf()
        protected set

    fun changeInfo(
        name: String,
        description: String?,
        type: UITemplateType,
        label: String?,
        price: Int,
        durationDays: Int,
        urlPath: String?,
        isActive: Boolean,
        isPopular: Boolean,
        displayOrder: Int,
    ) {
        this.name = name
        this.description = description
        this.type = type
        this.label = label
        this.price = price
        this.durationDays = durationDays
        this.urlPath = urlPath
        this.isActive = isActive
        this.isPopular = isPopular
        this.displayOrder = displayOrder
    }

    fun deactivate() {
        this.isActive = false
    }

    fun activate() {
        this.isActive = true
    }
}
