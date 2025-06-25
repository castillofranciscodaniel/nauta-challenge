package com.challenge.nauta_challenge.core.exception

class BookingDeferredException(val bookingNumber: String) : RuntimeException(
    "El booking $bookingNumber no pudo ser procesado en este momento y será reprocesado automáticamente."
)
