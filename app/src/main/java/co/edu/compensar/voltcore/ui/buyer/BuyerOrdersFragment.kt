package co.edu.compensar.voltcore.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import co.edu.compensar.voltcore.databinding.FragmentBuyerOrdersBinding
import co.edu.compensar.voltcore.databinding.ItemBuyerOrderBinding

class BuyerOrdersFragment : Fragment() {
    private var _binding: FragmentBuyerOrdersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBuyerOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mockOrders = listOf(
            Order("10293", "24/05/2024", 450000.0, "ENVIADO"),
            Order("10285", "20/05/2024", 120000.0, "ENTREGADO"),
            Order("10301", "26/05/2024", 89000.0, "PENDIENTE")
        )

        binding.rvBuyerOrders.adapter = OrderAdapter(mockOrders)
    }

    class OrderAdapter(private val orders: List<Order>) : RecyclerView.Adapter<OrderAdapter.ViewHolder>() {
        class ViewHolder(val binding: ItemBuyerOrderBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemBuyerOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val order = orders[position]
            with(holder.binding) {
                tvOrderNum.text = "Pedido #${order.id}"
                tvOrderDate.text = order.date
                tvOrderTotal.text = "Total: $ %,.0f".format(order.total)
                chipOrderStatus.text = order.status
                
                // Color por estado
                when(order.status) {
                    "PENDIENTE" -> {
                        chipOrderStatus.setChipBackgroundColorResource(android.R.color.holo_orange_light)
                        chipOrderStatus.setTextColor(holder.itemView.resources.getColor(android.R.color.white, null))
                    }
                    "ENVIADO" -> {
                        chipOrderStatus.setChipBackgroundColorResource(android.R.color.holo_blue_light)
                        chipOrderStatus.setTextColor(holder.itemView.resources.getColor(android.R.color.white, null))
                    }
                    "ENTREGADO" -> {
                        chipOrderStatus.setChipBackgroundColorResource(android.R.color.holo_green_light)
                        chipOrderStatus.setTextColor(holder.itemView.resources.getColor(android.R.color.white, null))
                    }
                }
            }
        }

        override fun getItemCount() = orders.size
    }

    data class Order(val id: String, val date: String, val total: Double, val status: String)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
