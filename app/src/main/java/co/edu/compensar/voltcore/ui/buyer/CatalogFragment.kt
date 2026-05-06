package co.edu.compensar.voltcore.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.data.CartManager
import co.edu.compensar.voltcore.databinding.FragmentCatalogBinding
import co.edu.compensar.voltcore.databinding.ItemProductCardBinding

class CatalogFragment : Fragment() {
    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        setupFakeCatalog()
        return binding.root
    }

    private fun setupFakeCatalog() {
        binding.rvCatalog.layoutManager = GridLayoutManager(context, 2)
        binding.rvCatalog.adapter = object : androidx.recyclerview.widget.RecyclerView.Adapter<CatalogViewHolder>() {
            private val items = listOf(
                Product("Motor 3000W", "$1.200.000"),
                Product("Casco Integral", "$450.000"),
                Product("Batería Litio", "$890.000"),
                Product("Frenos Disco", "$120.000"),
                Product("Llantas Sport", "$310.000"),
                Product("Controlador Pro", "$560.000")
            )

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatalogViewHolder {
                val b = ItemProductCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return CatalogViewHolder(b)
            }

            override fun onBindViewHolder(holder: CatalogViewHolder, position: Int) {
                val p = items[position]
                holder.binding.tvProductName.text = p.name
                holder.binding.tvProductPrice.text = p.price
                
                // Navegar al detalle
                holder.binding.root.setOnClickListener {
                    findNavController().navigate(R.id.productDetailFragment)
                }

                holder.binding.btnAddToCart.setOnClickListener {
                    CartManager.addItem(p.name)
                    Toast.makeText(context, "${p.name} añadido al carrito (${CartManager.getCount()} items)", Toast.LENGTH_SHORT).show()
                }
            }

            override fun getItemCount() = items.size
        }
    }

    class CatalogViewHolder(val binding: ItemProductCardBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)
    data class Product(val name: String, val price: String)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
