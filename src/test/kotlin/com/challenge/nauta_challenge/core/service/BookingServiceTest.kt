package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.objects.AssetUtilsTestObject
import com.challenge.nauta_challenge.core.repository.BookingRepository
import com.challenge.nauta_challenge.core.repository.ContainerRepository
import com.challenge.nauta_challenge.core.repository.InvoiceRepository
import com.challenge.nauta_challenge.core.repository.OrderRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest

class BookingServiceTest() {

    private val bookingRepository = mockk<BookingRepository>()
    private val containerRepository = mockk<ContainerRepository>()
    private val orderRepository = mockk<OrderRepository>()
    private val userLoggedService = mockk<UserLoggedService>()
    private val invoiceRepository = mockk<InvoiceRepository>()

    private val bookingService = BookingService(
        bookingRepository,
        containerRepository,
        orderRepository,
        userLoggedService,
        invoiceRepository
    )

    @Test
    fun `saveBooking ok when nothing exist`() {
        // When
        val userLoggged = AssetUtilsTestObject.getUser()
        val bookingNumber = "123456"
        val booking = AssetUtilsTestObject.getBookingWithoutId()

        val container_1 = AssetUtilsTestObject.getContainer_1()
        val container_2 = AssetUtilsTestObject.getContainer_2()

        val invoice_1 = AssetUtilsTestObject.getInvoice_1()
        val invoice_2 = AssetUtilsTestObject.getInvoice_2()
        val invoice_3 = AssetUtilsTestObject.getInvoice_3()
        val invoice_4 = AssetUtilsTestObject.getInvoice_4()

        val order_1 = AssetUtilsTestObject.getOrder_1()
        val order_2 = AssetUtilsTestObject.getOrder_2()


        coEvery { userLoggedService.getCurrentUserId() }.returns(userLoggged)

        coEvery { bookingRepository.findByBookingNumberAndUserId(bookingNumber, userLoggged.id!!) }
            .returns(null)

        coEvery { bookingRepository.save(booking) }.returns(booking.copy(id = 1))

        coEvery { containerRepository.save(container_1) }.returns(container_1.copy(id = 1))
        coEvery { containerRepository.save(container_2) }.returns(container_2.copy(id = 2))

        coEvery { orderRepository.save(order_1) }.returns(order_1.copy(id = 1))
        coEvery { orderRepository.save(order_2) }.returns(order_2.copy(id = 2))

        coEvery { invoiceRepository.save(invoice_1) }.returns(invoice_1.copy(id = 1))
        coEvery { invoiceRepository.save(invoice_2) }.returns(invoice_2.copy(id = 2))
        coEvery { invoiceRepository.save(invoice_3) }.returns(invoice_3.copy(id = 3))
        coEvery { invoiceRepository.save(invoice_4) }.returns(invoice_4.copy(id = 4))


        // Then

        val bookingSaved = runBlocking {
            bookingService.saveBooking(booking)
        }

        // Then
        assertEquals(1, bookingSaved.id)

        assertEquals(1, bookingSaved.containers.get(0).id)
        assertEquals(2, bookingSaved.containers.get(1).id)

        assertEquals(1, bookingSaved.orders.get(0).id)
        assertEquals(2, bookingSaved.orders.get(1).id)

        assertEquals(1, bookingSaved.orders.get(0).invoices.get(0).id)
        assertEquals(2, bookingSaved.orders.get(0).invoices.get(1).id)

        assertEquals(1, bookingSaved.orders.get(0).invoices.get(1).orderId)
        assertEquals(1, bookingSaved.orders.get(0).invoices.get(0).orderId)

        assertEquals(3, bookingSaved.orders.get(1).invoices.get(0).id)
        assertEquals(4, bookingSaved.orders.get(1).invoices.get(1).id)

        assertEquals(2, bookingSaved.orders.get(1).invoices.get(1).orderId)
        assertEquals(2, bookingSaved.orders.get(1).invoices.get(0).orderId)

    }
}