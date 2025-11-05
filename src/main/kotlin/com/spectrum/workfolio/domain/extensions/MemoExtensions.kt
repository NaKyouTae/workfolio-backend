package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.common.Memo
import com.spectrum.workfolio.utils.TimeUtil

fun Memo.toProto(): com.spectrum.workfolio.proto.common.Memo {
    val builder = com.spectrum.workfolio.proto.common.Memo.newBuilder()

    builder.setId(this.id)
    builder.setContent(this.content)
    builder.setTargetId(this.targetId)
    builder.setTargetType(
        com.spectrum.workfolio.proto.common.Memo.MemoTargetType.valueOf(this.targetType.name),
    )

    builder.setIsVisible(this.isVisible)
    builder.setPriority(this.priority)

    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
