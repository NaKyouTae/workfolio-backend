package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.payments.CreditHistory
import com.spectrum.workfolio.utils.TimeUtil

fun CreditHistory.toProto(includeWorker: Boolean = false): com.spectrum.workfolio.proto.common.CreditHistory {
    val builder = com.spectrum.workfolio.proto.common.CreditHistory.newBuilder()

    builder.setId(this.id)
    builder.setTxType(
        com.spectrum.workfolio.proto.common.CreditHistory.CreditTxType.valueOf(this.txType.name)
    )
    builder.setAmount(this.amount)
    builder.setBalanceBefore(this.balanceBefore)
    builder.setBalanceAfter(this.balanceAfter)
    if (this.referenceType != null) {
        builder.setReferenceType(this.referenceType)
    }
    if (this.referenceId != null) {
        builder.setReferenceId(this.referenceId)
    }
    if (this.description != null) {
        builder.setDescription(this.description)
    }
    if (includeWorker) {
        builder.setWorker(this.worker.toProto())
    }
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))

    return builder.build()
}
