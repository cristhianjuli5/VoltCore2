package co.edu.compensar.voltcore.data

object CartManager {
    private val cartItems = mutableListOf<String>()
    
    fun addItem(item: String) {
        cartItems.add(item)
    }
    
    fun getItems(): List<String> = cartItems
    
    fun getCount(): Int = cartItems.size
    
    fun clear() {
        cartItems.clear()
    }
}
