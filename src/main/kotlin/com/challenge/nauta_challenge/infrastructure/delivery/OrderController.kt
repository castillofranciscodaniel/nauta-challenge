package com.challenge.nauta_challenge.infrastructure.delivery

import com.challenge.nauta_challenge.core.model.Container
import com.challenge.nauta_challenge.core.model.Order
import com.challenge.nauta_challenge.core.service.OrderService
import kotlinx.coroutines.flow.Flow
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {

    @GetMapping()
    suspend fun getAllOrders(): Flow<Order> {
        return orderService.findAllOrdersForCurrentUser()
    }

    @GetMapping("/{purchaseId}/containers")
    suspend fun getContainersByOrderId(@PathVariable purchaseId: String): Flow<Container> {
        return orderService.findContainersByOrderId(purchaseId)
    }
}
