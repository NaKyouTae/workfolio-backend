package com.spectrum.workfolio.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

/**
 * AWS S3 클라이언트 설정 (Supabase Storage와 호환)
 * AWS SDK 2.x 사용
 */
@Configuration
class AwsS3Config(
    private val supabaseConfig: SupabaseConfig,
) {

    @Bean
    fun s3Client(): S3Client {
        // AWS 자격 증명 설정
        val credentials = AwsBasicCredentials.create(
            supabaseConfig.accessKey,
            supabaseConfig.secretKey,
        )

        // S3 Configuration (Supabase Storage 호환 설정)
        val s3Configuration = S3Configuration.builder()
            .pathStyleAccessEnabled(true)
            .checksumValidationEnabled(false)
            .chunkedEncodingEnabled(false)
            .build()

        // S3 클라이언트 빌드
        return S3Client.builder()
            .endpointOverride(URI.create(supabaseConfig.storageUrl))
            .region(Region.of(supabaseConfig.region))
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .serviceConfiguration(s3Configuration)
            .build()
    }
}
