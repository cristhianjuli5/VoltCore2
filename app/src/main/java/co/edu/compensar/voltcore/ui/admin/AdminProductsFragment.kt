package co.edu.compensar.voltcore.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import co.edu.compensar.voltcore.databinding.DialogEditProductBinding
import co.edu.compensar.voltcore.databinding.FragmentAdminProductsBinding
import co.edu.compensar.voltcore.databinding.ItemProductAdminBinding
import java.util.Locale

class AdminProductsFragment : Fragment() {
    private var _binding: FragmentAdminProductsBinding? = null
    private val binding get() = _binding!!
    
    private val productList = mutableListOf(
        Product(1, "Smartphone Volt X1", 899.99, 15, "Electrónica"),
        Product(2, "Audífonos Noise Cancel", 199.50, 24, "Audio"),
        Product(3, "Cargador Ultra Fast", 29.99, 50, "Accesorios")
    )

    private lateinit var adapter: ProductAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProductAdapter(productList, 
            onEdit = { product -> showProductDialog(product) },
            onDelete = { product -> deleteProduct(product) }
        )
        binding.rvProducts.adapter = adapter

        binding.fabAddProduct.setOnClickListener {
            showProductDialog()
        }
    }

    private fun showProductDialog(product: Product? = null) {
        val dialogBinding = DialogEditProductBinding.inflate(LayoutInflater.from(requireContext()))

        product?.let {
            dialogBinding.etProductName.setText(it.name)
            dialogBinding.etProductPrice.setText(it.price.toString())
            dialogBinding.etProductStock.setText(it.stock.toString())
            dialogBinding.etProductCategory.setText(it.category)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (product == null) "Agregar Producto" else "Editar Producto")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                val name = dialogBinding.etProductName.text.toString()
                val price = dialogBinding.etProductPrice.text.toString().toDoubleOrNull() ?: 0.0
                val stock = dialogBinding.etProductStock.text.toString().toIntOrNull() ?: 0
                val category = dialogBinding.etProductCategory.text.toString()

                if (name.isNotEmpty() && category.isNotEmpty()) {
                    if (product == null) {
                        val newId = (productList.maxOfOrNull { it.id } ?: 0) + 1
                        val newProduct = Product(newId, name, price, stock, category)
                        productList.add(0, newProduct)
                        adapter.notifyItemInserted(0)
                        binding.rvProducts.scrollToPosition(0)
                        Toast.makeText(requireContext(), "Producto creado", Toast.LENGTH_SHORT).show()
                    } else {
                        val index = productList.indexOfFirst { it.id == product.id }
                        if (index != -1) {
                            productList[index] = product.copy(name = name, price = price, stock = stock, category = category)
                            adapter.notifyItemChanged(index)
                            Toast.makeText(requireContext(), "Producto actualizado", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Por favor completa los campos obligatorios", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteProduct(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de que deseas eliminar ${product.name}?")
            .setPositiveButton("Eliminar") { _, _ ->
                val index = productList.indexOfFirst { it.id == product.id }
                if (index != -1) {
                    productList.removeAt(index)
                    adapter.notifyItemRemoved(index)
                    Toast.makeText(requireContext(), "Producto eliminado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    class ProductAdapter(
        private val products: List<Product>,
        private val onEdit: (Product) -> Unit,
        private val onDelete: (Product) -> Unit
    ) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

        class ProductViewHolder(val binding: ItemProductAdminBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val binding = ItemProductAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ProductViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            val product = products[position]
            with(holder.binding) {
                tvProductName.text = product.name
                tvProductPrice.text = String.format(Locale.getDefault(), "$ %.2f", product.price)
                tvProductStock.text = String.format(Locale.getDefault(), "Stock: %d | %s", product.stock, product.category)
                
                btnEditProduct.setOnClickListener { onEdit(product) }
                btnDeleteProduct.setOnClickListener { onDelete(product) }
            }
        }

        override fun getItemCount() = products.size
    }

    data class Product(val id: Int, val name: String, val price: Double, val stock: Int, val category: String)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
