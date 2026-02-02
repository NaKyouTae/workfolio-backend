package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.uitemplate.UiTemplatePlan
import com.spectrum.workfolio.domain.entity.uitemplate.UITemplate
import com.spectrum.workfolio.domain.entity.uitemplate.WorkerUITemplate
import com.spectrum.workfolio.domain.enums.UITemplateType
import com.spectrum.workfolio.utils.TimeUtil

fun UiTemplatePlan.toProto(): com.spectrum.workfolio.proto.common.UiTemplatePlan {
    return com.spectrum.workfolio.proto.common.UiTemplatePlan.newBuilder()
        .setId(this.id)
        .setDurationDays(this.durationDays)
        .setPrice(this.price)
        .setDisplayOrder(this.displayOrder)
        .build()
}

fun UITemplate.toProto(plans: List<UiTemplatePlan>? = null): com.spectrum.workfolio.proto.common.UITemplate {
    val builder = com.spectrum.workfolio.proto.common.UITemplate.newBuilder()

    builder.setId(this.id)
    builder.setName(this.name)
    if (this.description != null) {
        builder.setDescription(this.description)
    }
    builder.setType(this.type.toProtoType())
    builder.setPrice(this.price)
    builder.setDurationDays(this.durationDays)
    if (this.urlPath != null) {
        builder.setUrlPath(this.urlPath)
    }
    if (this.previewImageUrl != null) {
        builder.setPreviewImageUrl(this.previewImageUrl)
    }
    if (this.thumbnailUrl != null) {
        builder.setThumbnailUrl(this.thumbnailUrl)
    }
    builder.setIsActive(this.isActive)
    builder.setIsPopular(this.isPopular)
    builder.setDisplayOrder(this.displayOrder)
    if (!plans.isNullOrEmpty()) {
        builder.addAllPlans(plans.map { it.toProto() })
    }

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}

fun UITemplateType.toProtoType(): com.spectrum.workfolio.proto.common.UITemplate.UITemplateType {
    return when (this) {
        UITemplateType.URL -> com.spectrum.workfolio.proto.common.UITemplate.UITemplateType.URL
        UITemplateType.PDF -> com.spectrum.workfolio.proto.common.UITemplate.UITemplateType.PDF
    }
}

fun WorkerUITemplate.toProto(): com.spectrum.workfolio.proto.common.WorkerUITemplate {
    val builder = com.spectrum.workfolio.proto.common.WorkerUITemplate.newBuilder()

    builder.setId(this.id)
    builder.setPurchasedAt(TimeUtil.toEpochMilli(this.purchasedAt))
    builder.setExpiredAt(TimeUtil.toEpochMilli(this.expiredAt))
    builder.setCreditsUsed(this.creditsUsed)
    builder.setIsActive(this.isActive)
    builder.setIsExpired(this.isExpired())
    builder.setIsValid(this.isValid())

    builder.setWorker(this.worker.toProto())
    builder.setUiTemplate(this.uiTemplate.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
