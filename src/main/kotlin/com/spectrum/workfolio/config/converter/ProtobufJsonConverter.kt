package com.spectrum.workfolio.config.converter

import com.google.protobuf.util.JsonFormat

object ProtobufJsonConverter {
    val parser = JsonFormat.parser().ignoringUnknownFields()!!
    val printer = JsonFormat.printer().omittingInsignificantWhitespace().includingDefaultValueFields()!!
}

