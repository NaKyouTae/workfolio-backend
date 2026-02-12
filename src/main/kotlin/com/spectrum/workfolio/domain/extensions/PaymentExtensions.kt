package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.payments.Payment
import com.spectrum.workfolio.utils.TimeUtil

fun Payment.toProto(): com.spectrum.workfolio.proto.common.Payment {
    val builder = com.spectrum.workfolio.proto.common.Payment.newBuilder()

    builder.setId(this.id)
    builder.setStatus(this.status)
    builder.setType(this.type)
    builder.setTotalAmount(this.totalAmount.toLong())
    builder.setPaidAmount(this.paidAmount.toLong())
    builder.setCurrency(this.currency)
    if (this.metadata != null) {
        builder.setMetadata(this.metadata)
    }
    builder.setOrderId(this.orderId)
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
