package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.payments.PaymentTx
import com.spectrum.workfolio.utils.TimeUtil

fun PaymentTx.toProto(): com.spectrum.workfolio.proto.common.PaymentTx {
    val builder = com.spectrum.workfolio.proto.common.PaymentTx.newBuilder()

    builder.setId(this.id)
    builder.setTransactionType(
        try {
            com.spectrum.workfolio.proto.common.PaymentTx.TransactionType.valueOf(this.transactionType)
        } catch (e: IllegalArgumentException) {
            com.spectrum.workfolio.proto.common.PaymentTx.TransactionType.TYPE_UNKNOWN
        }
    )
    builder.setStatus(this.status)
    builder.setAmount(this.amount.toLong())
    if (this.transactionId != null) {
        builder.setTransactionId(this.transactionId)
    }
    if (this.requestData != null) {
        builder.setRequestData(this.requestData)
    }
    if (this.responseData != null) {
        builder.setResponseData(this.responseData)
    }
    if (this.errorMessage != null) {
        builder.setErrorMessage(this.errorMessage)
    }
    if (this.receiptUrl != null) {
        builder.setReceiptUrl(this.receiptUrl)
    }
    builder.setPaymentId(this.paymentId)
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))

    return builder.build()
}
