package com.challenge.nauta_challenge.infrastructure.repository.model

import com.challenge.nauta_challenge.core.model.User
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("users")
data class UserEntity(
    @Id
    val id: Long? = null,
    @Column("email") val email: String,
    @Column("password") val password: String,
    @Column("create_at") val createAt: LocalDateTime
) {
    companion object {
        fun fromModel(model: User) = UserEntity(
            id = model.id,
            email = model.email,
            password = model.password,
            createAt = model.createAt
        )
    }

    fun toModel() = User(
        id = this.id,
        email = this.email,
        password = this.password,
        createAt = this.createAt
    )
}
