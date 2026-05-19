package co.edu.compensar.voltcore.data

object CartManager {
    private val cartItems = mutableListOf<CartItem>()
    
    fun addItem(product: Product) {
        val existingItem = cartItems.find { it.productId == product.id }
        if (existingItem != null) {
            val index = cartItems.indexOf(existingItem)
            cartItems[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            cartItems.add(CartItem(
                productId = product.id,
                productName = product.name,
                price = product.price,
                quantity = 1,
                imageUrl = product.imageUrl,
                vendorId = product.vendorId
            ))
        }
    }

    fun removeItem(productId: String) {
        cartItems.removeAll { it.productId == productId }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        val existingItem = cartItems.find { it.productId == productId }
        if (existingItem != null) {
            val index = cartItems.indexOf(existingItem)
            if (quantity > 0) {
                cartItems[index] = existingItem.copy(quantity = quantity)
            } else {
                cartItems.removeAt(index)
            }
        }
    }
    
    fun getItems(): List<CartItem> = cartItems
    
    fun getCount(): Int = cartItems.sumOf { it.quantity }
    
    fun getTotalPrice(): Double = cartItems.sumOf { it.price * it.quantity }
    
    fun clear() {
        cartItems.clear()
    }
}
