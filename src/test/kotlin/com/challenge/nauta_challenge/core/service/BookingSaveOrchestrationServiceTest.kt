package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.objects.AssetUtilsTestObject
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
class BookingSaveOrchestrationServiceTest() {

    private val bookingService = mockk<BookingService>()
    private val userLoggedService = mockk<UserLoggedService>()
    private val containerService = mockk<ContainerService>()
    private val orderService = mockk<OrderService>()
    private val associationService = mockk<OrderContainerAssociationService>()

    private val bookingSaveOrchestrationService = BookingSaveOrchestrationService(
        bookingService,
        userLoggedService,
        containerService,
        orderService,
        associationService
    )

    @Test
    fun `saveBooking ok when nothing exist`() {
        // When
        val userLogged = AssetUtilsTestObject.getUser()
        val bookingNumber = "123456"
        val bookingId: Long = 1
        val booking = AssetUtilsTestObject.getBookingWithoutId()

        val container_1 = AssetUtilsTestObject.getContainer_1()
        val container_2 = AssetUtilsTestObject.getContainer_2()

        val invoice_1 = AssetUtilsTestObject.getInvoice_1()
        val invoice_2 = AssetUtilsTestObject.getInvoice_2()
        val invoice_3 = AssetUtilsTestObject.getInvoice_3()
        val invoice_4 = AssetUtilsTestObject.getInvoice_4()

        val order_1 = AssetUtilsTestObject.getOrder_1()
        val order_2 = AssetUtilsTestObject.getOrder_2()

        coEvery { userLoggedService.getCurrentUserId() }.returns(userLogged)

        coEvery { bookingService.findOrSaveBooking(booking, userLogged.id!!) }.returns(booking.copy(id = bookingId))

        val containersInput = listOf(container_1, container_2)
        val containersOutput = listOf(container_1.copy(id = 1), container_2.copy(id = 2))

        coEvery { containerService.saveContainersForBooking(containersInput, bookingId) }
            .returns(containersOutput)

        val ordersInput = listOf(order_1, order_2)
        val orderOutput = listOf(
            order_1.copy(id = 1, invoices = listOf(invoice_1.copy(id = 1), invoice_2.copy(id = 2))),
            order_2.copy(id = 2, invoices = listOf(invoice_3.copy(id = 3), invoice_4.copy(id = 4)))
        )

        coEvery { orderService.saveOrdersForBooking(ordersInput, bookingId) }.returns(orderOutput)

        coEvery {
            associationService.createAssociations(orderOutput, containersOutput, bookingNumber)
        }.returns(Unit)

        // Then
        val bookingSaved = runBlocking {
            bookingSaveOrchestrationService.saveBooking(booking)
        }

        // Then
        assertEquals(1, bookingSaved.userId)
        assertEquals(1, bookingSaved.id)

        assertEquals(1, bookingSaved.containers[0].id)
        assertEquals(2, bookingSaved.containers[1].id)

        assertEquals(1, bookingSaved.orders[0].id)
        assertEquals(2, bookingSaved.orders[1].id)

        assertEquals(1, bookingSaved.orders[0].invoices[0].id)
        assertEquals(2, bookingSaved.orders[0].invoices[1].id)

        assertEquals(1, bookingSaved.orders[0].invoices[1].orderId)
        assertEquals(1, bookingSaved.orders[0].invoices[0].orderId)

        assertEquals(3, bookingSaved.orders[1].invoices[0].id)
        assertEquals(4, bookingSaved.orders[1].invoices[1].id)

        assertEquals(2, bookingSaved.orders[1].invoices[1].orderId)
        assertEquals(2, bookingSaved.orders[1].invoices[0].orderId)
    }
}