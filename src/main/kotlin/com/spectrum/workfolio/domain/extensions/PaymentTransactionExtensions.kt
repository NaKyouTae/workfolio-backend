package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.payments.PaymentTransaction
import com.spectrum.workfolio.utils.TimeUtil

fun PaymentTransaction.toProto(): com.spectrum.workfolio.proto.common.PaymentTransaction {
    val builder = com.spectrum.workfolio.proto.common.PaymentTransaction.newBuilder()

    builder.setId(this.id)
    builder.setTransactionType(
        com.spectrum.workfolio.proto.common.PaymentTransaction.TransactionType.valueOf(this.transactionType.name)
    )
    builder.setStatus(this.status)
    builder.setAmount(this.amount)
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
