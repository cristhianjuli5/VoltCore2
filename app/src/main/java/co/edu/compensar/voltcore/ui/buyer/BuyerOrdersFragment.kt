package co.edu.compensar.voltcore.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBuyerOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = OrderAdapter(orderList)
        binding.rvBuyerOrders.adapter = adapter

        fetchOrders()
    }

    private fun fetchOrders() {
        val buyerId = auth.currentUser?.uid ?: return
        db.collection("orders")
            .whereEqualTo("buyerId", buyerId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Error al cargar pedidos", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    orderList.clear()
                    orderList.addAll(snapshot.toObjects(Order::class.java))
                    adapter.notifyDataSetChanged()
                }
            }
    }

    class OrderAdapter(private val orders: List<Order>) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {
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
                chipOrderStatus.text = order.status
                
                when(order.status) {
                    "PAID" -> {
                        chipOrderStatus.setChipBackgroundColorResource(android.R.color.holo_green_dark)
                    }
                    "PENDING" -> {
                        chipOrderStatus.setChipBackgroundColorResource(android.R.color.holo_orange_dark)
                    }
                }
            }
        }

        override fun getItemCount() = orders.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
