package com.spectrum.workfolio.domain.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

/**
 * 직장인
 */
@Entity
@Table(
    name = "worker",
    indexes = [
        Index(name = "idx_worker_name", columnList = "name"),
        Index(name = "idx_worker_nick_name", columnList = "nick_name"),
    ]
)
class Worker(
    name: String,
    nickName: String? = null,
): BaseEntity("WK") {
    @Column(name = "name", length = 256, nullable = false)
    var name: String = name
        protected set

    @Column(name = "nick_name", length = 512, nullable = false, unique = true)
    var nickName: String? = nickName
        protected set

    fun changeNickName(nickName: String) {
        this.nickName = nickName
    }
}
