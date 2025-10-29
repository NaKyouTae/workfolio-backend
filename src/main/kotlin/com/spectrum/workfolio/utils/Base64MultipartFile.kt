package com.spectrum.workfolio.utils

import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.*

/**
 * Base64로 인코딩된 문자열을 MultipartFile로 변환하는 유틸리티 클래스
 */
class Base64MultipartFile(
    private val base64Data: String,
    private val fileName: String,
    private val contentType: String = "application/octet-stream",
) : MultipartFile {

    private val decodedBytes: ByteArray by lazy {
        try {
            // Base64 디코딩 (data:image/png;base64, 접두사가 있으면 제거)
            val base64String = if (base64Data.contains(",")) {
                base64Data.substringAfter(",")
            } else {
                base64Data
            }
            Base64.getDecoder().decode(base64String)
        } catch (e: IllegalArgumentException) {
            throw WorkfolioException("Invalid Base64 data: ${e.message}")
        }
    }

    override fun getName(): String = "file"

    override fun getOriginalFilename(): String = fileName

    override fun getContentType(): String = contentType

    override fun isEmpty(): Boolean = decodedBytes.isEmpty()

    override fun getSize(): Long = decodedBytes.size.toLong()

    override fun getBytes(): ByteArray = decodedBytes

    override fun getInputStream(): InputStream = ByteArrayInputStream(decodedBytes)

    override fun transferTo(dest: File) {
        dest.writeBytes(decodedBytes)
    }
}
