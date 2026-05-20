package co.edu.compensar.voltcore.data

import java.util.Date

enum class UserRole {
    BUYER, VENDOR, ADMIN
}

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: UserRole = UserRole.BUYER,
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val imageUrl: String = "",
    val vendorId: String = "",
    val category: String = "",
    val status: String = "AVAILABLE" // AVAILABLE, INACTIVE
)

data class CartItem(
    val productId: String = "",
    val productName: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val imageUrl: String = "",
    val vendorId: String = ""
)

data class Order(
    val id: String = "",
    val buyerId: String = "",
    val items: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val status: String = "PENDING",
    val address: String = "",
    val timestamp: Date = Date(),
    val shippingGuide: String = "",
    val paymentMethod: String = "CARD",
    val paymentDetails: String = "" // Phone for wallet, or "CASH_CONVENIO" etc.
)
