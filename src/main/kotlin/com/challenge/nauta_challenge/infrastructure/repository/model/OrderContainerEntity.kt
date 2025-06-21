package com.challenge.nauta_challenge.infrastructure.repository.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table("order_containers")
data class OrderContainerEntity(
    @Id
    val id: Long? = null,

    @Column("order_id")
    val orderId: Long,

    @Column("container_id")
    val containerId: Long
) {
    companion object {
        fun fromModel(model: OrderContainerEntity) = OrderContainerEntity(
            id = model.id,
            orderId = model.orderId,
            containerId = model.containerId
        )
    }

    fun toModel() = OrderContainerEntity(
        id = this.id,
        orderId = this.orderId,
        containerId = this.containerId
    )
}