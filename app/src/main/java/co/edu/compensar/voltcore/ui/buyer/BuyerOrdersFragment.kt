package co.edu.compensar.voltcore.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.data.Order
import co.edu.compensar.voltcore.databinding.FragmentBuyerOrdersBinding
import co.edu.compensar.voltcore.databinding.ItemBuyerOrderBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Locale

class BuyerOrdersFragment : Fragment() {
    private var _binding: FragmentBuyerOrdersBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val orderList = mutableListOf<Order>()
    private lateinit var adapter: OrderAdapter
    private var snapshotListener: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBuyerOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvBuyerOrders.layoutManager = LinearLayoutManager(context)
        adapter = OrderAdapter(orderList) { order ->
            confirmDelivery(order)
        }
        binding.rvBuyerOrders.adapter = adapter

        fetchOrders()
    }

    private fun confirmDelivery(order: Order) {
        db.collection("orders").document(order.id)
            .update("status", "DELIVERED")
            .addOnSuccessListener {
                Toast.makeText(context, "Pedido marcado como recibido", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al confirmar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchOrders() {
        val buyerId = auth.currentUser?.uid ?: return
        snapshotListener = db.collection("orders")
            .whereEqualTo("buyerId", buyerId)
            .addSnapshotListener { snapshot, e ->
                if (_binding == null) return@addSnapshotListener
                if (e != null) {
                    android.util.Log.e("BuyerOrders", "Firestore Error: ${e.message}")
                    context?.let {
                        Toast.makeText(it, "Error al cargar pedidos", Toast.LENGTH_SHORT).show()
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    orderList.clear()
                    val orders = snapshot.toObjects(Order::class.java)
                        .sortedByDescending { it.timestamp }
                    orderList.addAll(orders)
                    adapter.notifyDataSetChanged()

                    if (orders.isEmpty()) {
                        binding.tvEmptyOrders.visibility = View.VISIBLE
                        binding.rvBuyerOrders.visibility = View.GONE
                    } else {
                        binding.tvEmptyOrders.visibility = View.GONE
                        binding.rvBuyerOrders.visibility = View.VISIBLE
                    }
                }
            }
    }

    class OrderAdapter(
        private val orders: List<Order>,
        private val onConfirmDelivery: (Order) -> Unit
    ) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {
        class ViewHolder(val binding: ItemBuyerOrderBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemBuyerOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val order = orders[position]
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            
            with(holder.binding) {
                tvOrderNum.text = "Pedido #${order.id.takeLast(6)}"
                tvOrderDate.text = dateFormat.format(order.timestamp)
                tvOrderTotal.text = "Total: $ %,.0f".format(order.total)
                
                // Mostrar items del pedido
                val itemsSummary = order.items.joinToString("\n") { "• ${it.quantity}x ${it.productName}" }
                tvOrderItems.text = itemsSummary
                
                // Reset visibility for recycling
                tvShippingGuide.visibility = if (order.shippingGuide.isNotEmpty()) View.VISIBLE else View.GONE
                tvShippingGuide.text = "Guía de envío: ${order.shippingGuide}"
                
                btnConfirmDelivery.visibility = if (order.status == "SHIPPED") View.VISIBLE else View.GONE
                btnConfirmDelivery.setOnClickListener { onConfirmDelivery(order) }

                // Pipeline logic
                val activeColor = ContextCompat.getColor(holder.itemView.context, R.color.volt_primary)
                val inactiveColor = ContextCompat.getColor(holder.itemView.context, R.color.glass_stroke)

                viewStep1.setBackgroundColor(inactiveColor)
                viewStep2.setBackgroundColor(inactiveColor)
                viewStep3.setBackgroundColor(inactiveColor)

                when(order.status.uppercase()) {
                    "PENDING", "PAID" -> {
                        chipOrderStatus.text = "PAGADO"
                        chipOrderStatus.setChipBackgroundColorResource(android.R.color.holo_blue_dark)
                        viewStep1.setBackgroundColor(activeColor)
                        tvStatusDescription.text = "Pago verificado. Pendiente por el vendedor."
                    }
                    "ACCEPTED" -> {
                        chipOrderStatus.text = "ACEPTADO"
                        chipOrderStatus.setChipBackgroundColorResource(android.R.color.holo_orange_dark)
                        viewStep1.setBackgroundColor(activeColor)
                        viewStep2.setBackgroundColor(activeColor)
                        tvStatusDescription.text = "El vendedor está preparando tu pedido."
                    }
                    "SHIPPED" -> {
                        chipOrderStatus.text = "ENVIADO"
                        chipOrderStatus.setChipBackgroundColorResource(android.R.color.holo_green_dark)
                        viewStep1.setBackgroundColor(activeColor)
                        viewStep2.setBackgroundColor(activeColor)
                        viewStep3.setBackgroundColor(activeColor)
                        tvStatusDescription.text = "¡Tu pedido va en camino!"
                    }
                    "DELIVERED" -> {
                        chipOrderStatus.text = "ENTREGADO"
                        chipOrderStatus.setChipBackgroundColorResource(android.R.color.darker_gray)
                        viewStep1.setBackgroundColor(activeColor)
                        viewStep2.setBackgroundColor(activeColor)
                        viewStep3.setBackgroundColor(activeColor)
                        tvStatusDescription.text = "El pedido ha sido entregado."
                    }
                    else -> {
                        chipOrderStatus.text = order.status
                        chipOrderStatus.setChipBackgroundColorResource(android.R.color.darker_gray)
                        tvStatusDescription.text = "Estado: ${order.status}"
                    }
                }
            }
        }

        override fun getItemCount() = orders.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snapshotListener?.remove()
        _binding = null
    }
}
