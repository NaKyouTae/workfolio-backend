package com.spectrum.workfolio.services

import com.spectrum.workfolio.domain.dto.AttachmentCreateDto
import com.spectrum.workfolio.domain.dto.AttachmentUpdateDto
import com.spectrum.workfolio.domain.entity.common.Attachment
import com.spectrum.workfolio.domain.entity.resume.Resume
import com.spectrum.workfolio.domain.enums.AttachmentCategory
import com.spectrum.workfolio.domain.enums.AttachmentTargetType
import com.spectrum.workfolio.domain.enums.AttachmentType
import com.spectrum.workfolio.domain.repository.AttachmentRepository
import com.spectrum.workfolio.proto.attachment.AttachmentRequest
import com.spectrum.workfolio.utils.EntityTypeValidator
import com.spectrum.workfolio.utils.FileUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AttachmentCommandService(
    private val fileUploadService: FileUploadService,
    private val attachmentRepository: AttachmentRepository,
    private val attachmentQueryService: AttachmentQueryService,
) {

    private val logger = LoggerFactory.getLogger(AttachmentCommandService::class.java)

    /**
     * 첨부파일 생성 (트랜잭션 분리)
     * 1. 먼저 트랜잭션 안에서 Attachment 엔티티만 저장 (fileUrl은 빈 문자열)
     * 2. 트랜잭션 커밋 후 파일 업로드 실행 (Connection 점유 없음)
     * 3. 업로드 성공 시 fileUrl 업데이트
     * 4. 업로드 실패 시 Attachment 삭제
     */
    @Transactional  // 전역 타임아웃(30초) 적용
    fun createAttachment(dto: AttachmentCreateDto): Attachment {
        // 먼저 Attachment 생성하여 ID 획득 (fileUrl은 빈 문자열로 저장)
        val attachment = Attachment(
            type = dto.type,
            category = dto.category,
            fileName = dto.fileName ?: "",
            fileUrl = "",  // 파일 업로드는 트랜잭션 밖에서 처리
            url = dto.url ?: "",
            isVisible = dto.isVisible,
            priority = dto.priority,
            targetId = dto.targetId,
            targetType = dto.targetType,
        )
        val savedAttachment = attachmentRepository.save(attachment)

        // fileUrl이 이미 제공된 경우 (fileData 없음)
        if (dto.fileUrl != null && dto.fileUrl.isNotBlank()) {
            savedAttachment.changeFileUrl(dto.fileUrl)
            return savedAttachment
        }

        // fileData가 있으면 트랜잭션 커밋 후 파일 업로드 실행
        if (dto.fileData != null && !dto.fileData.isEmpty) {
            // 트랜잭션 밖에서 파일 업로드 실행
            uploadFileAfterCommit(savedAttachment, dto.fileData, dto.fileName, dto.storagePath)
        }

        return savedAttachment
    }

    /**
     * 트랜잭션 커밋 후 파일 업로드 실행
     * Connection을 점유하지 않으므로 Connection leak 문제 해결
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    fun uploadFileAfterCommit(
        attachment: Attachment,
        fileData: com.google.protobuf.ByteString,
        fileName: String?,
        storagePath: String,
    ) {
        try {
            // 파일 확장자 추출
            val extension = FileUtil.extractFileExtension(fileName ?: attachment.fileName)
            val storageFileName = "${attachment.id}.$extension"

            // Supabase Storage에 업로드 (트랜잭션 밖에서 실행)
            val uploadedFileUrl = fileUploadService.uploadFileToStorage(
                fileData = fileData,
                fileName = storageFileName,
                storagePath = storagePath,
            )

            // 업로드 성공 시 fileUrl 업데이트 (새로운 트랜잭션)
            attachment.changeFileUrl(uploadedFileUrl)
            attachmentRepository.save(attachment)
            
            logger.info("✅ 파일 업로드 완료: attachmentId=${attachment.id}, fileUrl=$uploadedFileUrl")
        } catch (e: Exception) {
            logger.error("❌ 파일 업로드 실패: attachmentId=${attachment.id}, error=${e.message}", e)
            
            // 업로드 실패 시 Attachment 삭제 (새로운 트랜잭션)
            try {
                attachmentRepository.delete(attachment)
                logger.info("✅ 업로드 실패로 인한 Attachment 삭제 완료: attachmentId=${attachment.id}")
            } catch (deleteException: Exception) {
                logger.error("❌ Attachment 삭제 실패: attachmentId=${attachment.id}, error=${deleteException.message}", deleteException)
            }
            
            throw e
        }
    }

    /**
     * Bulk 첨부파일 생성 (트랜잭션 분리)
     * 1. 먼저 트랜잭션 안에서 모든 Attachment 엔티티 저장
     * 2. 트랜잭션 커밋 후 각 파일 업로드 실행
     */
    @Transactional(timeout = 30)  // DB 저장만 하므로 짧은 타임아웃
    fun createBulkAttachment(
        targetType: AttachmentTargetType,
        targetId: String,
        storagePath: String,
        requests: List<AttachmentRequest>,
    ): List<Attachment> {
        // 먼저 모든 Attachment 엔티티 저장 (fileUrl은 빈 문자열 또는 제공된 URL)
        val savedAttachments = requests.map { request ->
            val attachment = Attachment(
                type = AttachmentType.valueOf(request.type.name),
                category = AttachmentCategory.valueOf(request.category.name),
                fileName = request.fileName,
                fileUrl = request.fileUrl,  // fileUrl이 제공된 경우 사용
                url = request.url,
                isVisible = request.isVisible,
                priority = request.priority,
                targetId = targetId,
                targetType = targetType,
            )
            attachmentRepository.save(attachment)
        }

        // fileData가 있는 Attachment만 트랜잭션 밖에서 업로드
        savedAttachments.forEachIndexed { index, attachment ->
            val request = requests[index]
            if (request.hasFileData() && !request.fileData.isEmpty) {
                // 트랜잭션 밖에서 파일 업로드 실행
                uploadFileAfterCommit(
                    attachment = attachment,
                    fileData = request.fileData,
                    fileName = request.fileName,
                    storagePath = "$storagePath/$targetId",
                )
            }
        }

        return savedAttachments
    }

    /**
     * 엔티티로부터 Bulk 첨부파일 생성 (트랜잭션 분리)
     * 1. 먼저 트랜잭션 안에서 모든 Attachment 엔티티 저장
     * 2. 트랜잭션 커밋 후 각 파일 복사 실행
     */
    @Transactional(timeout = 30)  // DB 저장만 하므로 짧은 타임아웃
    fun createBulkAttachmentFromEntity(
        entity: Any,
        storagePath: String,
        attachments: List<Attachment>,
    ) {
        val resume = EntityTypeValidator.requireEntityType<Resume>(entity)

        val savedAttachments = attachments.map { originalAttachment ->
            val newAttachment = Attachment(
                fileName = originalAttachment.fileName,
                fileUrl = "",  // 파일 복사는 트랜잭션 밖에서 처리
                url = originalAttachment.url,
                isVisible = originalAttachment.isVisible,
                priority = originalAttachment.priority,
                type = originalAttachment.type,
                category = originalAttachment.category,
                targetId = originalAttachment.targetId,
                targetType = originalAttachment.targetType,
            )
            attachmentRepository.save(newAttachment)
        }

        // 트랜잭션 밖에서 파일 복사 실행
        savedAttachments.forEachIndexed { index, savedAttachment ->
            val originalAttachment = attachments[index]
            if (originalAttachment.fileUrl.isNotBlank()) {
                copyFileAfterCommit(
                    attachment = savedAttachment,
                    sourceFileUrl = originalAttachment.fileUrl,
                    storagePath = "$storagePath/${resume.id}",
                )
            } else {
                logger.info("No file to copy for attachment: ${originalAttachment.id} (fileUrl is blank)")
            }
        }
    }

    /**
     * 트랜잭션 커밋 후 파일 복사 실행
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    fun copyFileAfterCommit(
        attachment: Attachment,
        sourceFileUrl: String,
        storagePath: String,
    ) {
        try {
            val extension = attachment.fileName.substringAfterLast(".", "")
            val newFileName = "${attachment.id}.$extension"

            val copiedFileUrl = fileUploadService.copyFileInStorage(
                sourceFileUrl = sourceFileUrl,
                destinationFileName = newFileName,
                destinationStoragePath = storagePath,
            )

            // 복사 성공 시 fileUrl 업데이트 (새로운 트랜잭션)
            attachment.changeFileUrl(copiedFileUrl)
            attachmentRepository.save(attachment)
            
            logger.info("✅ 파일 복사 완료: attachmentId=${attachment.id}, fileUrl=$copiedFileUrl")
        } catch (e: Exception) {
            logger.error("❌ 파일 복사 실패: attachmentId=${attachment.id}, error=${e.message}", e)
            // 복사 실패 시 원본 fileUrl 사용
            attachment.changeFileUrl(sourceFileUrl)
            attachmentRepository.save(attachment)
        }
    }

    /**
     * Bulk 첨부파일 업데이트 (트랜잭션 분리)
     * 1. 먼저 트랜잭션 안에서 Attachment 정보만 업데이트
     * 2. 트랜잭션 커밋 후 파일 업로드/삭제 실행
     */
    @Transactional(timeout = 30)  // DB 업데이트만 하므로 짧은 타임아웃
    fun updateBulkAttachment(
        targetType: AttachmentTargetType,
        targetId: String,
        storagePath: String,
        requests: List<AttachmentRequest>,
    ): List<Attachment> {
        val existingAttachments = attachmentRepository.findByTargetIdAndTargetTypeOrderByPriorityAsc(targetId, targetType)

        val requestMap = requests
            .filter { it.id.isNotBlank() }
            .associateBy { it.id }

        val updatedEntities = existingAttachments.mapNotNull { attachment ->
            requestMap[attachment.id]?.let { request ->
                // fileData가 없고 fileUrl이 제공된 경우만 즉시 업데이트
                val fileUrl = if (!request.hasFileData() || request.fileData.isEmpty) {
                    request.fileUrl
                } else {
                    // fileData가 있으면 일단 기존 fileUrl 유지 (업로드는 트랜잭션 밖에서)
                    attachment.fileUrl
                }

                attachment.changeInfo(
                    type = AttachmentType.valueOf(request.type.name),
                    category = AttachmentCategory.valueOf(request.category.name),
                    fileName = request.fileName,
                    fileUrl = fileUrl,
                    url = request.url,
                    isVisible = request.isVisible,
                    priority = request.priority,
                )
                attachment
            }
        }

        val savedAttachments = attachmentRepository.saveAll(updatedEntities)

        // fileData가 있는 Attachment만 트랜잭션 밖에서 업로드
        savedAttachments.forEach { attachment ->
            val request = requestMap[attachment.id]
            if (request != null && request.hasFileData() && !request.fileData.isEmpty) {
                // 트랜잭션 밖에서 파일 업로드 및 기존 파일 삭제
                updateFileAfterCommit(
                    attachment = attachment,
                    fileData = request.fileData,
                    fileName = request.fileName,
                    storagePath = "$storagePath/$targetId",
                )
            }
        }

        return savedAttachments
    }

    /**
     * 트랜잭션 커밋 후 파일 업데이트 실행
     */
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    fun updateFileAfterCommit(
        attachment: Attachment,
        fileData: com.google.protobuf.ByteString,
        fileName: String,
        storagePath: String,
    ) {
        try {
            val extension = FileUtil.extractFileExtension(fileName)
            val storageFileName = "${attachment.id}.$extension"

            // 새 파일 업로드 (트랜잭션 밖에서 실행)
            val newFileUrl = fileUploadService.uploadFileToStorage(
                fileData = fileData,
                fileName = storageFileName,
                storagePath = storagePath,
            )

            // 기존 파일 삭제 (트랜잭션 밖에서 실행)
            fileUploadService.deleteFileFromStorage(listOf(attachment))

            // 업로드 성공 시 fileUrl 업데이트 (새로운 트랜잭션)
            attachment.changeFileUrl(newFileUrl)
            attachmentRepository.save(attachment)
            
            logger.info("✅ 파일 업데이트 완료: attachmentId=${attachment.id}, fileUrl=$newFileUrl")
        } catch (e: Exception) {
            logger.error("❌ 파일 업데이트 실패: attachmentId=${attachment.id}, error=${e.message}", e)
            throw e
        }
    }

    /**
     * 첨부파일 업데이트 (트랜잭션 분리)
     * 1. 먼저 트랜잭션 안에서 Attachment 정보만 업데이트
     * 2. 트랜잭션 커밋 후 파일 업로드/삭제 실행
     */
    @Transactional  // 전역 타임아웃(30초) 적용
    fun updateAttachment(dto: AttachmentUpdateDto): Attachment {
        val attachment = attachmentQueryService.getAttachment(dto.id)

        // fileData가 없고 fileUrl이 제공된 경우만 즉시 업데이트
        val fileUrl = if (dto.fileData == null || dto.fileData.isEmpty) {
            dto.fileUrl ?: attachment.fileUrl
        } else {
            // fileData가 있으면 일단 기존 fileUrl 유지 (업로드는 트랜잭션 밖에서)
            attachment.fileUrl
        }

        attachment.changeInfo(
            type = dto.type,
            category = dto.category,
            fileName = dto.fileName ?: "",
            fileUrl = fileUrl,
            url = dto.url ?: "",
            isVisible = dto.isVisible,
            priority = dto.priority,
        )

        // fileData가 있으면 트랜잭션 밖에서 파일 업로드 실행
        if (dto.fileData != null && !dto.fileData.isEmpty) {
            updateFileAfterCommit(
                attachment = attachment,
                fileData = dto.fileData,
                fileName = dto.fileName ?: attachment.fileName,
                storagePath = dto.storagePath,
            )
        }

        return attachment
    }

    @Transactional  // 전역 타임아웃(30초) 적용
    fun deleteAttachments(attachmentIds: List<String>) {
        if (attachmentIds.isEmpty()) {
            return
        }

        // ID로 Attachment 조회하여 파일 URL 가져오기
        val attachments = attachmentRepository.findAllById(attachmentIds)

        fileUploadService.deleteFileFromStorage(attachments)

        // DB에서 Attachment 삭제
        attachmentRepository.deleteAllById(attachmentIds)
    }
}
