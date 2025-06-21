package com.challenge.nauta_challenge.core.objects

import com.challenge.nauta_challenge.core.model.*

object AssetUtilsTestObject {

    fun getUser() = User(id = 1, email = "test@gmail.com")

    fun getBookingWithoutId() = Booking(
        bookingNumber = "123456",
        userId = 1,
        containers = listOf(getContainer_1(), getContainer_2()),
        orders = listOf(getOrder_1(), getOrder_2())

    )

    fun getContainer_1() = Container(containerNumber = "container_1")
    fun getContainer_2() = Container(containerNumber = "container_2")

    fun getInvoice_1() = Invoice(invoiceNumber = "invoice_1", orderId = 1)
    fun getInvoice_2() = Invoice(invoiceNumber = "invoice_2", orderId = 1)
    fun getInvoice_3() = Invoice(invoiceNumber = "invoice_3", orderId = 2)
    fun getInvoice_4() = Invoice(invoiceNumber = "invoice_4", orderId = 2)

    fun getOrder_1() = Order(purchaseNumber = "123", invoices = listOf(getInvoice_1(), getInvoice_2()))
    fun getOrder_2() = Order(purchaseNumber = "456", invoices = listOf(getInvoice_3(), getInvoice_4()))

}