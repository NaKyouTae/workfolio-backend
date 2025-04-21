package com.spectrum.workfolio.domain.entity

import jakarta.persistence.*
import org.hibernate.proxy.HibernateProxy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.domain.Persistable
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*
import kotlin.jvm.Transient

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity(
    prefix: String = "",
    createdAt: LocalDateTime = LocalDateTime.now(),
    updatedAt: LocalDateTime = LocalDateTime.now(),
): Persistable<String> {
    @Id
    @Column(length = 16, nullable = false, updatable = false)
    private val id: String = generateUUID(prefix)

    @CreatedDate
    @Column(name = "created_At", updatable = false)
    var createdAt: LocalDateTime = createdAt

    @LastModifiedDate
    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = updatedAt

    @Transient
    private var _isNew = true

    override fun getId(): String = id

    override fun isNew(): Boolean = _isNew

    override fun equals(other: Any?): Boolean {
        if (other == null) {
            return false
        }

        if (other !is HibernateProxy && this::class != other::class) {
            return false
        }

        return id == getIdentifier(other)
    }

    private fun getIdentifier(obj: Any): Serializable {
        return if (obj is HibernateProxy) {
            obj.hibernateLazyInitializer.identifier as Serializable
        } else {
            (obj as BaseEntity).id
        }
    }

    override fun hashCode() = Objects.hashCode(id)

    @PostPersist
    @PostLoad
    protected fun load() {
        _isNew = false
    }

    private fun generateUUID(prefix: String): String {
        val sf = UUID.randomUUID().toString().replace("-", "").substring(0, 14).uppercase()
        return prefix + sf
    }
}
