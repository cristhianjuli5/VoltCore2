package co.edu.compensar.voltcore.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import co.edu.compensar.voltcore.R
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
    private var snapshotListener: com.google.firebase.firestore.ListenerRegistration? = null

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
        // Contar usuarios
        db.collection("users").get().addOnSuccessListener { snapshot ->
            if (_binding == null) return@addOnSuccessListener
            binding.tvUserCount.text = snapshot.size().toString()
        }

        // Contar productos
        db.collection("products").get().addOnSuccessListener { snapshot ->
            if (_binding == null) return@addOnSuccessListener
            binding.tvProductCount.text = snapshot.size().toString()
            
            var totalInventory = 0.0
            snapshot.toObjects(Product::class.java).forEach { 
                totalInventory += (it.price * it.stock)
            }
            binding.tvInventoryValue.text = getString(R.string.price_format, totalInventory)
        }

        // Estadísticas de Pedidos
        db.collection("orders").get().addOnSuccessListener { snapshot ->
            if (_binding == null) return@addOnSuccessListener
            val orders = snapshot.toObjects(Order::class.java)
            binding.tvOrderCountReport.text = orders.size.toString()

            val totalSales = orders.sumOf { it.total }
            binding.tvTotalSales.text = getString(R.string.price_format, totalSales)

            val avgTicket = if (orders.isNotEmpty()) totalSales / orders.size else 0.0
            binding.tvAvgTicket.text = getString(R.string.price_format, avgTicket)

            // Alertas Sospechosas (ej. pedidos > 5M)
            val suspiciousOrders = orders.filter { it.total > 5000000 }
            if (suspiciousOrders.isNotEmpty()) {
                binding.cvSuspicious.visibility = View.VISIBLE
                binding.tvSuspiciousCount.text = getString(R.string.suspicious_activity_msg, suspiciousOrders.size)
            } else {
                binding.cvSuspicious.visibility = View.GONE
            }
        }
    }

    private fun fetchRecentOrders() {
        snapshotListener = db.collection("orders")
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
            val context = holder.itemView.context
            
            holder.text1.text = context.getString(R.string.order_label, order.id.take(8).uppercase())
            holder.text1.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.volt_text_primary))
            
            val totalFormatted = context.getString(R.string.price_format, order.total)
            holder.text2.text = context.getString(R.string.order_summary_format, totalFormatted, order.status)
            holder.text2.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.volt_text_secondary))
        }

        override fun getItemCount() = orders.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snapshotListener?.remove()
        _binding = null
    }
}
