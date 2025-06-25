package com.challenge.nauta_challenge.core.service

import com.challenge.nauta_challenge.core.exception.BookingDeferredException
import com.challenge.nauta_challenge.core.objects.AssetUtilsTestObject
import com.challenge.nauta_challenge.core.repository.MessagePublisherRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@SpringBootTest
class BookingSaveOrchestrationServiceTest() {

    private val bookingService = mockk<BookingService>()
    private val userLoggedService = mockk<UserLoggedService>()
    private val containerService = mockk<ContainerService>()
    private val orderService = mockk<OrderService>()
    private val associationService = mockk<OrderContainerAssociationService>()
    private val messagePublisher = mockk<MessagePublisherRepository>(relaxed = true)

    private val bookingSaveOrchestrationService = BookingSaveOrchestrationService(
        bookingService,
        userLoggedService,
        containerService,
        orderService,
        associationService,
        messagePublisher
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

        coEvery { userLoggedService.getCurrentUser() }.returns(userLogged)

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

    @Test
    fun propagaExcepcionCuandoGetCurrentUserFalla() = runTest {
        // Arrange
        val booking = AssetUtilsTestObject.getBookingWithoutId()

        coEvery { userLoggedService.getCurrentUser() } throws Exception("Usuario no autenticado")

        // Act & Assert
        val exception = assertFailsWith<BookingDeferredException> {
            bookingSaveOrchestrationService.saveBooking(booking)
        }

        assertEquals("El booking ${booking.bookingNumber} no pudo ser procesado en este momento y será reprocesado automáticamente.", exception.message)
    }

    @Test
    fun propagaExcepcionCuandoFindOrSaveBookingFalla() = runTest {
        // Arrange
        val booking = AssetUtilsTestObject.getBookingWithoutId()
        val userLogged = AssetUtilsTestObject.getUser()

        coEvery { userLoggedService.getCurrentUser() } returns userLogged
        coEvery { bookingService.findOrSaveBooking(booking, userLogged.id!!) } throws Exception("Error al guardar booking")

        // Act & Assert
        val exception = assertFailsWith<BookingDeferredException> {
            bookingSaveOrchestrationService.saveBooking(booking)
        }

        assertEquals("El booking ${booking.bookingNumber} no pudo ser procesado en este momento y será reprocesado automáticamente.", exception.message)
    }

    @Test
    fun propagaExcepcionCuandoSaveContainersForBookingFalla() = runTest {
        // Arrange
        val booking = AssetUtilsTestObject.getBookingWithoutId()
        val userLogged = AssetUtilsTestObject.getUser()
        val bookingId: Long = 1

        coEvery { userLoggedService.getCurrentUser() } returns userLogged
        coEvery { bookingService.findOrSaveBooking(booking, userLogged.id!!) } returns booking.copy(id = bookingId)
        coEvery { containerService.saveContainersForBooking(booking.containers, bookingId) } throws Exception("Error al guardar contenedores")

        // Act & Assert
        val exception = assertFailsWith<BookingDeferredException> {
            bookingSaveOrchestrationService.saveBooking(booking)
        }

        assertEquals("El booking ${booking.bookingNumber} no pudo ser procesado en este momento y será reprocesado automáticamente.", exception.message)
    }

    @Test
    fun propagaExcepcionCuandoSaveOrdersForBookingFalla() = runTest {
        // Arrange
        val booking = AssetUtilsTestObject.getBookingWithoutId()
        val userLogged = AssetUtilsTestObject.getUser()
        val bookingId: Long = 1
        val containersOutput = booking.containers.mapIndexed { index, container -> container.copy(id = index.toLong() + 1) }

        coEvery { userLoggedService.getCurrentUser() } returns userLogged
        coEvery { bookingService.findOrSaveBooking(booking, userLogged.id!!) } returns booking.copy(id = bookingId)
        coEvery { containerService.saveContainersForBooking(booking.containers, bookingId) } returns containersOutput
        coEvery { orderService.saveOrdersForBooking(booking.orders, bookingId) } throws Exception("Error al guardar órdenes")

        // Act & Assert
        val exception = assertFailsWith<BookingDeferredException> {
            bookingSaveOrchestrationService.saveBooking(booking)
        }

        assertEquals("El booking ${booking.bookingNumber} no pudo ser procesado en este momento y será reprocesado automáticamente.", exception.message)
    }

    @Test
    fun propagaExcepcionCuandoCreateAssociationsFalla() = runTest {
        // Arrange
        val booking = AssetUtilsTestObject.getBookingWithoutId()
        val userLogged = AssetUtilsTestObject.getUser()
        val bookingId: Long = 1

        val containersOutput = booking.containers.mapIndexed { index, container -> container.copy(id = index.toLong() + 1) }
        val ordersOutput = booking.orders.mapIndexed { index, order -> order.copy(id = index.toLong() + 1) }

        coEvery { userLoggedService.getCurrentUser() } returns userLogged
        coEvery { bookingService.findOrSaveBooking(booking, userLogged.id!!) } returns booking.copy(id = bookingId)
        coEvery { containerService.saveContainersForBooking(booking.containers, bookingId) } returns containersOutput
        coEvery { orderService.saveOrdersForBooking(booking.orders, bookingId) } returns ordersOutput
        coEvery { associationService.createAssociations(ordersOutput, containersOutput, booking.bookingNumber) } throws Exception("Error al crear asociaciones")

        // Act & Assert
        val exception = assertFailsWith<BookingDeferredException> {
            bookingSaveOrchestrationService.saveBooking(booking)
        }

        assertEquals("El booking ${booking.bookingNumber} no pudo ser procesado en este momento y será reprocesado automáticamente.", exception.message)
    }

    @Test
    fun `usa booking userId si getCurrentUser falla`() = runTest {
        // Arrange
        val booking = AssetUtilsTestObject.getBookingWithoutId().copy(userId = 99)
        coEvery { userLoggedService.getCurrentUser() } throws Exception("Fallo auth")
        coEvery { bookingService.findOrSaveBooking(booking, 99) } returns booking.copy(id = 1)
        coEvery { containerService.saveContainersForBooking(any(), any()) } returns emptyList()
        coEvery { orderService.saveOrdersForBooking(any(), any()) } returns emptyList()
        coEvery { associationService.createAssociations(any(), any(), any()) } returns Unit

        // Act
        val bookingSaved = bookingSaveOrchestrationService.saveBooking(booking)

        // Assert
        assertEquals(1, bookingSaved.id)
        assertEquals(99, bookingSaved.userId)
    }

    @Test
    fun publicaEnMessagePublisherSiOcurreExcepcionYPropagaBookingDeferredException() = runTest {
        // Arrange
        val booking = AssetUtilsTestObject.getBookingWithoutId().copy(userId = 77)
        coEvery { userLoggedService.getCurrentUser() } returns AssetUtilsTestObject.getUser()
        coEvery { bookingService.findOrSaveBooking(any(), any()) } throws RuntimeException("fallo DB")
        coEvery { messagePublisher.publishMessage(any(), any()) } returns Unit

        // Act & Assert
        val exception = assertFailsWith<BookingDeferredException> {
            bookingSaveOrchestrationService.saveBooking(booking)
        }
        assertEquals("El booking ${booking.bookingNumber} no pudo ser procesado en este momento y será reprocesado automáticamente.", exception.message)
        io.mockk.coVerify { messagePublisher.publishMessage(any(), any()) }
    }

    @Test
    fun propagaExcepcionCuandoSimulatedFailureOcurre() = runTest {
        // Arrange
        val booking = AssetUtilsTestObject.getBookingWithoutId()
        val userLogged = AssetUtilsTestObject.getUser()

        coEvery { userLoggedService.getCurrentUser() } returns userLogged
        coEvery { bookingService.findOrSaveBooking(any(), any()) } throws RuntimeException("Simulated failure during booking save process")

        // Act & Assert
        val exception = assertFailsWith<BookingDeferredException> {
            bookingSaveOrchestrationService.saveBooking(booking)
        }

        assertEquals("El booking ${booking.bookingNumber} no pudo ser procesado en este momento y será reprocesado automáticamente.", exception.message)
    }
}