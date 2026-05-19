package co.edu.compensar.voltcore.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import co.edu.compensar.voltcore.data.Order
import co.edu.compensar.voltcore.data.Product
import co.edu.compensar.voltcore.databinding.FragmentAdminReportsBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class AdminReportsFragment : Fragment() {
    private var _binding: FragmentAdminReportsBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val recentOrders = mutableListOf<Order>()
    private lateinit var adapter: OrdersAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        adapter = OrdersAdapter(recentOrders)
        binding.rvRecentOrders.adapter = adapter
        
        fetchRealStats()
        fetchRecentOrders()
    }

    private fun fetchRealStats() {
        // Contar usuarios reales
        db.collection("users").get().addOnSuccessListener { snapshot ->
            if (_binding == null) return@addOnSuccessListener
            binding.tvUserCount.text = snapshot.size().toString()
        }

        // Contar productos reales
        db.collection("products").get().addOnSuccessListener { snapshot ->
            if (_binding == null) return@addOnSuccessListener
            binding.tvProductCount.text = snapshot.size().toString()
        }

        // Calcular valor total de inventario
        db.collection("products").get().addOnSuccessListener { snapshot ->
            if (_binding == null) return@addOnSuccessListener
            var totalValue = 0.0
            for (doc in snapshot.documents) {
                val p = doc.toObject(Product::class.java)
                if (p != null) {
                    totalValue += (p.price * p.stock)
                }
            }
            binding.tvInventoryValue.text = String.format(Locale.getDefault(), "$ %,.0f", totalValue)
        }
    }

    private fun fetchRecentOrders() {
        db.collection("orders")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, e ->
                if (_binding == null) return@addSnapshotListener
                if (snapshot != null) {
                    recentOrders.clear()
                    recentOrders.addAll(snapshot.toObjects(Order::class.java))
                    adapter.notifyDataSetChanged()
                    
                    binding.tvNoOrders.visibility = if (recentOrders.isEmpty()) View.VISIBLE else View.GONE
                }
            }
    }

    class OrdersAdapter(private val orders: List<Order>) : RecyclerView.Adapter<OrdersAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text1: TextView = view.findViewById(android.R.id.text1)
            val text2: TextView = view.findViewById(android.R.id.text2)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_2, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val order = orders[position]
            holder.text1.text = "Pedido: ${order.id.take(8).uppercase()}"
            holder.text1.setTextColor(android.graphics.Color.WHITE)
            holder.text2.text = "Total: ${String.format(Locale.getDefault(), "$ %,.0f", order.total)} - Estado: ${order.status}"
            holder.text2.setTextColor(android.graphics.Color.parseColor("#B3FFFFFF"))
        }

        override fun getItemCount() = orders.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
