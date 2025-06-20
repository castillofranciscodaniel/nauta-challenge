package com.challenge.nauta_challenge.adapters.repositoty

import com.challenge.nauta_challenge.core.model.User
import com.challenge.nauta_challenge.core.repository.BookingRepository
import com.challenge.nauta_challenge.core.repository.UserRepository
import com.challenge.nauta_challenge.infrastructure.repository.dao.BookingDao
import com.challenge.nauta_challenge.infrastructure.repository.dao.UserDao
import com.challenge.nauta_challenge.infrastructure.repository.model.UserEntity
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class BookingRepositoryImpl(
    private val bookingDao: BookingDao
) : BookingRepository {

    

}
