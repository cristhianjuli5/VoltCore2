package co.edu.compensar.voltcore.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.data.CartManager
import co.edu.compensar.voltcore.databinding.FragmentCartBinding

class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        
        setupCartList()
        
        binding.btnGoToCheckout.setOnClickListener {
            if (CartManager.getCount() > 0) {
                findNavController().navigate(R.id.checkoutFragment)
            } else {
                android.widget.Toast.makeText(context, "El carrito está vacío", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        updateTotal()
        
        return binding.root
    }

    private fun setupCartList() {
        binding.rvCartItems.layoutManager = LinearLayoutManager(context)
        binding.rvCartItems.adapter = object : RecyclerView.Adapter<CartViewHolder>() {
            private val items = CartManager.getItems()

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
                return CartViewHolder(view)
            }

            override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
                val item = items[position]
                holder.textView.text = item
                holder.textView.setTextColor(resources.getColor(R.color.white, null))
            }

            override fun getItemCount() = items.size
        }
    }

    private fun updateTotal() {
        val total = CartManager.getCount() * 100000 // Precio ficticio promedio
        binding.tvTotal.text = "$ $total"
    }

    class CartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(android.R.id.text1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
