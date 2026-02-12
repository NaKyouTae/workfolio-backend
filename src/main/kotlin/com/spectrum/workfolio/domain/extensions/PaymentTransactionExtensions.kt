package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.payments.PaymentTx
import com.spectrum.workfolio.utils.TimeUtil

fun PaymentTx.toProto(): com.spectrum.workfolio.proto.common.PaymentTransaction {
    val builder = com.spectrum.workfolio.proto.common.PaymentTransaction.newBuilder()

    builder.setId(this.id)
    builder.setStatus(this.status)
    builder.setTargetType(this.targetType)
    builder.setTargetIds(this.targetIds)
    builder.setPgType(this.pgType)
    if (this.pgTxId != null) {
        builder.setPgTxId(this.pgTxId)
    }
    builder.setAmount(this.amount.toLong())
    builder.setPaidAmount(this.paidAmount.toLong())
    if (this.providerId != null) {
        builder.setProviderId(this.providerId)
    }
    if (this.parentTxId != null) {
        builder.setParentTxId(this.parentTxId)
    }
    if (this.customerId != null) {
        builder.setCustomerId(this.customerId)
    }
    if (this.receiptUrl != null) {
        builder.setReceiptUrl(this.receiptUrl)
    }
    if (this.approveNumber != null) {
        builder.setApproveNumber(this.approveNumber)
    }
    if (this.paymentMethod != null) {
        builder.setPaymentMethod(this.paymentMethod)
    }
    if (this.cardCompany != null) {
        builder.setCardCompany(this.cardCompany)
    }
    if (this.cardNumber != null) {
        builder.setCardNumber(this.cardNumber)
    }
    if (this.installmentMonth != null) {
        builder.setInstallmentMonth(this.installmentMonth!!)
    }
    builder.setRetryCount(this.retryCount)
    builder.setMaxRetryCount(this.maxRetryCount)
    if (this.previousTxId != null) {
        builder.setPreviousTxId(this.previousTxId)
    }
    if (this.requestData != null) {
        builder.setRequestData(this.requestData)
    }
    if (this.responseData != null) {
        builder.setResponseData(this.responseData)
    }
    if (this.responseCode != null) {
        builder.setResponseCode(this.responseCode)
    }
    if (this.failedReason != null) {
        builder.setFailedReason(this.failedReason)
    }
    if (this.reason != null) {
        builder.setReason(this.reason)
    }
    if (this.metadata != null) {
        builder.setMetadata(this.metadata)
    }
    if (this.succeededAt != null) {
        builder.setSucceededAt(TimeUtil.toEpochMilli(this.succeededAt!!))
    }
    if (this.failedAt != null) {
        builder.setFailedAt(TimeUtil.toEpochMilli(this.failedAt!!))
    }
    builder.setPaymentId(this.paymentId)
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))

    return builder.build()
}
