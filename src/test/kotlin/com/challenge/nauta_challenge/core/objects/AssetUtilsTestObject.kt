package com.challenge.nauta_challenge.core.objects

import com.challenge.nauta_challenge.core.model.Booking
import com.challenge.nauta_challenge.core.model.User

object AssetUtilsTestObject {

    fun getUser() = User(id = 1, email = "test@gmail.com")

    fun getBookingWithoutId() = Booking(
        bookingNumber = "123456",
        userId = 1
    )

}