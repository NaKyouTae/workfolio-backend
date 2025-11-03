package com.spectrum.workfolio.utils

object FileUtil {
    /**
     * 파일 확장자 추출
     * @param fileName 원본 파일 이름 (예: "portfolio.pdf")
     * @return 확장자 (예: "pdf"), 없으면 "bin"
     */
    fun extractFileExtension(fileName: String?): String {
        if (fileName.isNullOrBlank()) return "bin"

        val extension = fileName.substringAfterLast(".", "")
        return if (extension.isNotBlank() && extension.length <= 10) {
            extension.lowercase()
        } else {
            "bin"
        }
    }
}
