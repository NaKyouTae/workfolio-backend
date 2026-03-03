package com.spectrum.workfolio.services

import com.google.protobuf.ByteString
import com.spectrum.workfolio.domain.entity.Image
import com.spectrum.workfolio.domain.enums.ImageExtType
import com.spectrum.workfolio.domain.enums.ImageStatus
import com.spectrum.workfolio.domain.enums.ImageTargetType
import com.spectrum.workfolio.domain.repository.ImageRepository
import com.spectrum.workfolio.utils.WorkfolioException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Service
class ImageService(
    private val imageRepository: ImageRepository,
    private val supabaseStorageService: SupabaseStorageService,
    private val fileUploadService: FileUploadService,
) {

    @Transactional(readOnly = true)
    fun getImagesByTarget(targetType: ImageTargetType, targetId: String): List<Image> {
        return imageRepository.findByTargetTypeAndTargetIdAndStatusOrderByPriorityAsc(
            targetType, targetId, ImageStatus.ACTIVE,
        )
    }

    @Transactional(readOnly = true)
    fun getImagesByTargetAndExtType(
        targetType: ImageTargetType,
        targetId: String,
        extType: ImageExtType,
    ): List<Image> {
        return imageRepository.findByTargetTypeAndTargetIdAndExtTypeAndStatusOrderByPriorityAsc(
            targetType, targetId, extType, ImageStatus.ACTIVE,
        )
    }

    @Transactional(readOnly = true)
    fun getImagesByTargetIds(targetType: ImageTargetType, targetIds: List<String>): List<Image> {
        if (targetIds.isEmpty()) return emptyList()
        return imageRepository.findByTargetTypeAndTargetIdInAndStatusOrderByPriorityAsc(
            targetType, targetIds, ImageStatus.ACTIVE,
        )
    }

    /**
     * MultipartFile 방식으로 이미지 업로드 (어드민 UI 템플릿 이미지용)
     */
    @Transactional
    fun uploadImages(
        targetType: ImageTargetType,
        targetId: String,
        extType: ImageExtType,
        files: List<MultipartFile>,
    ): List<Image> {
        val existingImages = getImagesByTarget(targetType, targetId)
        var nextOrder = if (existingImages.isEmpty()) 0 else existingImages.maxOf { it.priority } + 1
        val storagePath = getStoragePath(targetType, targetId)

        return files.map { file ->
            val fileName = "${UUID.randomUUID()}_${file.originalFilename}"
            val imageUrl = supabaseStorageService.uploadFile(file, fileName, storagePath)

            val image = Image(
                targetType = targetType,
                targetId = targetId,
                extType = extType,
                url = imageUrl,
                priority = nextOrder++,
            )
            imageRepository.save(image)
        }
    }

    /**
     * ByteString(base64) 방식으로 프로필 이미지 업로드
     * 기존 프로필 이미지가 있으면 삭제 후 새로 업로드
     */
    @Transactional
    fun uploadProfileImage(
        targetId: String,
        imageData: ByteString,
        fileName: String,
    ): Image {
        // 기존 프로필 이미지 삭제
        deleteImagesByTarget(ImageTargetType.PROFILE, targetId)

        val storagePath = getStoragePath(ImageTargetType.PROFILE, targetId)
        val uniqueFileName = "${UUID.randomUUID()}_$fileName"
        val imageUrl = fileUploadService.uploadFileToStorage(imageData, uniqueFileName, storagePath)

        val image = Image(
            targetType = ImageTargetType.PROFILE,
            targetId = targetId,
            extType = ImageExtType.PROFILE,
            url = imageUrl,
            priority = 0,
        )
        return imageRepository.save(image)
    }

    @Transactional
    fun deleteImage(imageId: String) {
        val image = imageRepository.findById(imageId)
            .orElseThrow { WorkfolioException("이미지를 찾을 수 없습니다.") }
        supabaseStorageService.deleteFileByUrl(image.url)
        imageRepository.delete(image)
    }

    @Transactional
    fun deleteImagesByTarget(targetType: ImageTargetType, targetId: String) {
        val images = getImagesByTarget(targetType, targetId)
        images.forEach { image ->
            supabaseStorageService.deleteFileByUrl(image.url)
        }
        if (images.isNotEmpty()) {
            imageRepository.deleteAllInBatch(images)
        }
    }

    /**
     * 프로필 이미지 URL 조회 (단일 이미지)
     */
    @Transactional(readOnly = true)
    fun getProfileImageUrl(targetId: String): String? {
        val images = getImagesByTarget(ImageTargetType.PROFILE, targetId)
        return images.firstOrNull()?.url
    }

    /**
     * 여러 대상의 프로필 이미지 URL을 일괄 조회
     * @return targetId -> imageUrl 매핑
     */
    @Transactional(readOnly = true)
    fun getProfileImageUrls(targetIds: List<String>): Map<String, String> {
        if (targetIds.isEmpty()) return emptyMap()
        val images = getImagesByTargetIds(ImageTargetType.PROFILE, targetIds)
        return images.associate { it.targetId to it.url }
    }

    /**
     * 프로필 이미지 복제 (이력서 복제 시)
     */
    @Transactional
    fun copyProfileImage(originalImage: Image, newTargetId: String): Image {
        val storagePath = getStoragePath(ImageTargetType.PROFILE, newTargetId)
        val fileName = "${UUID.randomUUID()}_profile.jpg"
        val newUrl = fileUploadService.copyFileInStorage(originalImage.url, fileName, storagePath)

        val image = Image(
            targetType = ImageTargetType.PROFILE,
            targetId = newTargetId,
            extType = ImageExtType.PROFILE,
            url = newUrl,
            priority = 0,
        )
        return imageRepository.save(image)
    }

    private fun getStoragePath(targetType: ImageTargetType, targetId: String): String {
        return when (targetType) {
            ImageTargetType.UI_TEMPLATE -> "ui-templates/$targetId"
            ImageTargetType.PROFILE -> "resumes/profiles/$targetId"
        }
    }
}
