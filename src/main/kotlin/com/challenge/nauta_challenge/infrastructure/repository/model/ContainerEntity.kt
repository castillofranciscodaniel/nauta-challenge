package com.challenge.nauta_challenge.infrastructure.repository.model

import com.challenge.nauta_challenge.core.model.Container
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("containers")
data class ContainerEntity(
    @Id
    val id: Long? = null,
    @Column("container_number") val containerNumber: String,
    @Column("booking_id") val bookingId: Long?
) {
    companion object {
        fun fromModel(model: Container) = ContainerEntity(
            id = model.id,
            containerNumber = model.containerNumber,
            bookingId = model.bookingId
        )
    }

    fun toModel() = Container(
        id = this.id,
        containerNumber = this.containerNumber,
        bookingId = this.bookingId
    )
}
