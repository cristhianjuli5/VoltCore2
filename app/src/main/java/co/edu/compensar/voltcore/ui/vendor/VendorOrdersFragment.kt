package co.edu.compensar.voltcore.ui.vendor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import co.edu.compensar.voltcore.databinding.FragmentVendorOrdersBinding
import co.edu.compensar.voltcore.databinding.ItemVendorOrderBinding

class VendorOrdersFragment : Fragment() {
    private var _binding: FragmentVendorOrdersBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVendorOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mockOrders = listOf(
            VendorOrder("VC-8291", "Juan Pérez", "20/05/2024", "PENDIENTE"),
            VendorOrder("VC-9310", "Maria Gomez", "21/05/2024", "ENVIADO")
        )

        binding.rvOrders.adapter = VendorOrderAdapter(mockOrders)
    }

    class VendorOrderAdapter(private val orders: List<VendorOrder>) : RecyclerView.Adapter<VendorOrderAdapter.ViewHolder>() {
        
        class ViewHolder(val binding: ItemVendorOrderBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemVendorOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val order = orders[position]
            with(holder.binding) {
                tvOrderId.text = "Orden #${order.id}"
                tvCustomerName.text = "Comprador: ${order.customer}"
                tvOrderDate.text = "Fecha: ${order.date}"
                chipStatus.text = order.status
                
                if (order.status == "ENVIADO") {
                    layoutExpanded.visibility = View.GONE
                } else {
                    layoutExpanded.visibility = View.VISIBLE
                }

                btnMarkAsShipped.setOnClickListener {
                    val guide = etShippingGuide.text.toString()
                    if (guide.isNotEmpty()) {
                        Toast.makeText(root.context, "Orden ${order.id} enviada con guía $guide", Toast.LENGTH_SHORT).show()
                    } else {
                        etShippingGuide.error = "Ingresa la guía"
                    }
                }
            }
        }

        override fun getItemCount() = orders.size
    }

    data class VendorOrder(val id: String, val customer: String, val date: String, val status: String)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
