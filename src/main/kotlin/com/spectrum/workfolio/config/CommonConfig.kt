package com.spectrum.workfolio.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.google.protobuf.Message
import com.spectrum.workfolio.config.converter.ProtobufJsonConverter
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.protobuf.ProtobufHttpMessageConverter
import org.springframework.http.converter.protobuf.ProtobufJsonFormatHttpMessageConverter
import org.springframework.scheduling.annotation.EnableScheduling
import java.io.IOException

@Configuration
@EnableJpaAuditing
@EnableScheduling
class CommonConfig {
    @Bean
    fun protobufHttpMessageConverter(): ProtobufHttpMessageConverter {
        return ProtobufJsonFormatHttpMessageConverter(ProtobufJsonConverter.parser, ProtobufJsonConverter.printer)
    }

    @Bean
    fun jackson2ObjectMapperBuilderCustomizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { o: Jackson2ObjectMapperBuilder ->
            o.serializerByType(
                Message::class.java,
                object : JsonSerializer<Message>() {
                    @Throws(IOException::class)
                    override fun serialize(
                        value: Message?,
                        gen: JsonGenerator,
                        serializers: SerializerProvider,
                    ) {
                        gen.writeRawValue(ProtobufJsonConverter.printer.print(value))
                    }
                },
            )
        }
    }
}
