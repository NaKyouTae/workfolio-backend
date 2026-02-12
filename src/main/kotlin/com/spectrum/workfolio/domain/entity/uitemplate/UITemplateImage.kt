package com.spectrum.workfolio.domain.entity.uitemplate

import com.spectrum.workfolio.domain.entity.BaseEntity
import com.spectrum.workfolio.domain.enums.UITemplateImageType
import jakarta.persistence.*

@Entity
@Table(
    name = "ui_template_images",
    indexes = [
        Index(name = "idx_ui_template_images_template_type_order", columnList = "ui_template_id, image_type, display_order"),
    ],
)
class UITemplateImage(
    uiTemplate: UITemplate,
    imageType: UITemplateImageType,
    imageUrl: String,
    displayOrder: Int = 0,
) : BaseEntity("TI") {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ui_template_id", nullable = false)
    var uiTemplate: UITemplate = uiTemplate
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", length = 32, nullable = false)
    var imageType: UITemplateImageType = imageType
        protected set

    @Column(name = "image_url", columnDefinition = "TEXT", nullable = false)
    var imageUrl: String = imageUrl
        protected set

    @Column(name = "display_order", nullable = false)
    var displayOrder: Int = displayOrder
        protected set

    fun changeInfo(
        imageType: UITemplateImageType,
        imageUrl: String,
        displayOrder: Int,
    ) {
        this.imageType = imageType
        this.imageUrl = imageUrl
        this.displayOrder = displayOrder
    }
}
