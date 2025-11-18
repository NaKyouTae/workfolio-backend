package com.spectrum.workfolio.domain.extensions

import com.spectrum.workfolio.domain.entity.record.RecordGroup
import com.spectrum.workfolio.utils.TimeUtil

fun RecordGroup.toProto(): com.spectrum.workfolio.proto.common.RecordGroup {
    val builder = com.spectrum.workfolio.proto.common.RecordGroup.newBuilder()

    builder.setId(this.id)
    builder.setType(com.spectrum.workfolio.proto.common.RecordGroup.RecordGroupType.valueOf(this.type.name))
    builder.setTitle(this.title)
    builder.setIsDefault(this.isDefault)
    builder.setPublicId(this.publicId)
    builder.setColor(this.color)
    builder.setPriority(this.priority)
    builder.setDefaultRole(com.spectrum.workfolio.proto.common.RecordGroup.RecordGroupRole.valueOf(this.defaultRole.name))
    builder.setWorker(this.worker.toProto())
    builder.setCreatedAt(TimeUtil.toEpochMilli(this.createdAt))
    builder.setUpdatedAt(TimeUtil.toEpochMilli(this.updatedAt))

    return builder.build()
}
