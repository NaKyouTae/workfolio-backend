package com.spectrum.workfolio.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

/**
 * 관리자
 */
@Entity
@Table(
    name = "staffs",
    indexes = [
        Index(name = "idx_staffs_username", columnList = "username"),
        Index(name = "idx_staffs_email", columnList = "email"),
    ],
)
class Staff(
    username: String,
    password: String,
    name: String,
    email: String,
    phone: String? = null,
    isActive: Boolean = true,
) : BaseEntity("SF") {

    @Column(name = "username", length = 64, nullable = false, unique = true)
    var username: String = username
        protected set

    @Column(name = "password", length = 256, nullable = false)
    var password: String = password
        protected set

    @Column(name = "name", length = 128, nullable = false)
    var name: String = name
        protected set

    @Column(name = "email", length = 256, nullable = false, unique = true)
    var email: String = email
        protected set

    @Column(name = "phone", length = 32, nullable = true)
    var phone: String? = phone
        protected set

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = isActive
        protected set

    fun changeInfo(
        name: String,
        email: String,
        phone: String?,
    ) {
        this.name = name
        this.email = email
        this.phone = phone
    }

    fun changePassword(password: String) {
        this.password = password
    }

    fun changeActive(isActive: Boolean) {
        this.isActive = isActive
    }
}

