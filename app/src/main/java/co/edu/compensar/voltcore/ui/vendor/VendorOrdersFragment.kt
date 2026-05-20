package co.edu.compensar.voltcore.ui.vendor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.compensar.voltcore.data.Order
import co.edu.compensar.voltcore.databinding.FragmentVendorOrdersBinding
import co.edu.compensar.voltcore.databinding.ItemVendorOrderBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class VendorOrdersFragment : Fragment() {
    private var _binding: FragmentVendorOrdersBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val orderList = mutableListOf<Order>()
    private lateinit var adapter: VendorOrderAdapter
    private var snapshotListener: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVendorOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = VendorOrderAdapter(orderList) { order, newStatus, guide ->
            updateOrderStatus(order, newStatus, guide)
        }
        binding.rvOrders.layoutManager = LinearLayoutManager(context)
        binding.rvOrders.adapter = adapter

        fetchOrders()
    }

    private fun fetchOrders() {
        val vendorId = auth.currentUser?.uid ?: return
        
        snapshotListener = db.collection("orders")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (_binding == null) return@addSnapshotListener
                if (e != null) {
                    context?.let {
                        Toast.makeText(it, "Error al cargar pedidos", Toast.LENGTH_SHORT).show()
                    }
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    orderList.clear()
                    for (doc in snapshot.documents) {
                        val order = doc.toObject(Order::class.java)
                        // Filtrar: solo mostrar si el pedido contiene al menos un producto de este vendedor
                        if (order != null && order.items.any { it.vendorId == vendorId }) {
                            orderList.add(order.copy(id = doc.id))
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun updateOrderStatus(order: Order, status: String, shippingGuide: String = "") {
        val updates = mutableMapOf<String, Any>("status" to status)
        if (shippingGuide.isNotEmpty()) {
            updates["shippingGuide"] = shippingGuide
        }

        db.collection("orders").document(order.id)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(context, "Estado actualizado a $status", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al actualizar estado", Toast.LENGTH_SHORT).show()
            }
    }

    class VendorOrderAdapter(
        private val orders: List<Order>,
        private val onStatusUpdate: (Order, String, String) -> Unit
    ) : RecyclerView.Adapter<VendorOrderAdapter.ViewHolder>() {
        
        class ViewHolder(val binding: ItemVendorOrderBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemVendorOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val order = orders[position]
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            
            with(holder.binding) {
                tvOrderId.text = "Orden #${order.id.takeLast(8)}"
                tvCustomerName.text = "Destino: ${order.address}"
                tvOrderDate.text = "Fecha: ${sdf.format(order.timestamp)}"
                chipStatus.text = order.status
                
                // Mostrar los productos del pedido que pertenecen a este vendedor
                val currentVendorId = FirebaseAuth.getInstance().currentUser?.uid
                val vendorItems = order.items.filter { it.vendorId == currentVendorId }
                tvOrderItems.text = vendorItems.joinToString("\n") { "${it.quantity}x ${it.productName}" }
                
                when(order.status) {
                    "PAID" -> {
                        chipStatus.text = "PAGADO"
                        layoutExpanded.visibility = View.VISIBLE
                        btnAcceptOrder.visibility = View.VISIBLE
                        tilShippingGuide.visibility = View.GONE
                        btnMarkAsShipped.visibility = View.GONE
                    }
                    "ACCEPTED" -> {
                        chipStatus.text = "ACEPTADO"
                        layoutExpanded.visibility = View.VISIBLE
                        btnAcceptOrder.visibility = View.GONE
                        tilShippingGuide.visibility = View.VISIBLE
                        btnMarkAsShipped.visibility = View.VISIBLE
                    }
                    "SHIPPED" -> {
                        chipStatus.text = "ENVIADO"
                        layoutExpanded.visibility = View.GONE
                    }
                    else -> {
                        chipStatus.text = order.status
                        layoutExpanded.visibility = View.GONE
                    }
                }

                btnAcceptOrder.setOnClickListener {
                    onStatusUpdate(order, "ACCEPTED", "")
                }

                btnMarkAsShipped.setOnClickListener {
                    val guide = etShippingGuide.text.toString()
                    if (guide.isNotEmpty()) {
                        onStatusUpdate(order, "SHIPPED", guide)
                    } else {
                        etShippingGuide.error = "Ingresa la guía de envío"
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
