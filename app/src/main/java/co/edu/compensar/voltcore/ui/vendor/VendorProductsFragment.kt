package co.edu.compensar.voltcore.ui.vendor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.databinding.FragmentVendorProductsBinding
import co.edu.compensar.voltcore.databinding.ItemVendorProductBinding

class VendorProductsFragment : Fragment() {
    private var _binding: FragmentVendorProductsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVendorProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mockProducts = mutableListOf(
            VendorProduct("Filtro de Aceite XP", 45000.0, "ACTIVO"),
            VendorProduct("Batería 12V Volt", 280000.0, "ACTIVO"),
            VendorProduct("Pastillas de Freno", 120000.0, "EN REVISIÓN")
        )

        val adapter = VendorProductAdapter(mockProducts) { product ->
            showArchiveDialog(product)
        }
        binding.rvVendorProducts.adapter = adapter

        binding.fabAddProduct.setOnClickListener {
            findNavController().navigate(R.id.action_vendorProducts_to_form)
        }
    }

    private fun showArchiveDialog(product: VendorProduct) {
        AlertDialog.Builder(requireContext())
            .setTitle("Archivar Producto")
            .setMessage("¿Deseas archivar '${product.name}'? Ya no será visible para los compradores.")
            .setPositiveButton("Archivar") { _, _ ->
                Toast.makeText(context, "${product.name} archivado (Soft Delete)", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    class VendorProductAdapter(
        private val products: List<VendorProduct>,
        private val onArchive: (VendorProduct) -> Unit
    ) : RecyclerView.Adapter<VendorProductAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemVendorProductBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemVendorProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val product = products[position]
            with(holder.binding) {
                tvProductName.text = product.name
                tvProductPrice.text = String.format("$ %,.0f", product.price)
                chipStatus.text = product.status
                
                btnArchive.setOnClickListener { onArchive(product) }
            }
        }

        override fun getItemCount() = products.size
    }

    data class VendorProduct(val name: String, val price: Double, val status: String)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
