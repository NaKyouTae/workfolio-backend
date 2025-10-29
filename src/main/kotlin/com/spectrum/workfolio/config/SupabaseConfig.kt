package com.spectrum.workfolio.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "supabase")
data class SupabaseConfig(
    var url: String = "",
    var storageUrl: String = "",
    var region: String = "",
    var accessKey: String = "",
    var secretKey: String = "",
)
