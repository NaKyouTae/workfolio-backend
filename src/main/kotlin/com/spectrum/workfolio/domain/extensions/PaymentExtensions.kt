package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.payments.Payment
import com.spectrum.workfolio.utils.TimeUtil

fun Payment.toProto(): com.spectrum.workfolio.proto.common.Payment {
    val builder = com.spectrum.workfolio.proto.common.Payment.newBuilder()

    builder.setId(this.id)
    builder.setAmount(this.amount)
    builder.setCurrency(this.currency.name)
    builder.setStatus(com.spectrum.workfolio.proto.common.Payment.PaymentStatus.valueOf(this.status.name))
    builder.setPaymentMethod(com.spectrum.workfolio.proto.common.Payment.PaymentMethod.valueOf(this.paymentMethod.name))
    builder.setPaymentProvider(this.paymentProvider)
    builder.setProviderPaymentId(this.providerPaymentId)
    if (this.paidAt != null) {
        builder.setPaidAt(TimeUtil.toEpochMilli(this.paidAt!!))
    }
    if (this.refundedAt != null) {
        builder.setRefundedAt(TimeUtil.toEpochMilli(this.refundedAt!!))
    }
    builder.setRefundAmount(this.refundAmount)
    if (this.refundReason != null) {
        builder.setRefundReason(this.refundReason)
    }
    if (this.failureReason != null) {
        builder.setFailureReason(this.failureReason)
    }
    if (this.failureCode != null) {
        builder.setFailureCode(this.failureCode)
    }
    if (this.metadataJson != null) {
        builder.setMetadataJson(this.metadataJson)
    }
    builder.setWorker(this.worker.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
