package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.turnover.CheckList
import com.spectrum.workfolio.utils.TimeUtil

fun CheckList.toProto(): com.spectrum.workfolio.proto.common.CheckList {
    val builder = com.spectrum.workfolio.proto.common.CheckList.newBuilder()

    builder.setId(this.id)
    builder.setChecked(this.checked)
    builder.setContent(this.content)

    builder.setIsVisible(this.isVisible)
    builder.setPriority(this.priority)

    builder.setTurnOver(this.turnOver.toProto())

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}

fun CheckList.toWithoutTurnOverGoalProto(): com.spectrum.workfolio.proto.common.CheckList {
    val builder = com.spectrum.workfolio.proto.common.CheckList.newBuilder()

    builder.setId(this.id)
    builder.setChecked(this.checked)
    builder.setContent(this.content)

    builder.setIsVisible(this.isVisible)
    builder.setPriority(this.priority)

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
