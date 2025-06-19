package com.challenge.nauta_challenge.core.model

import java.time.LocalDateTime

data class User(
    val id: Long? = null,
    val email: String,
    val password: String,
    val createAt: LocalDateTime = LocalDateTime.now()
)
