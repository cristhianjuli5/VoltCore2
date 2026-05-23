package co.edu.compensar.voltcore.ui.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import co.edu.compensar.voltcore.R
import androidx.recyclerview.widget.RecyclerView
import co.edu.compensar.voltcore.data.Product
import co.edu.compensar.voltcore.databinding.DialogEditProductBinding
import co.edu.compensar.voltcore.databinding.FragmentAdminProductsBinding
import co.edu.compensar.voltcore.databinding.ItemProductAdminBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class AdminProductsFragment : Fragment() {
    private var _binding: FragmentAdminProductsBinding? = null
    private val binding get() = _binding!!
    
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val productList = mutableListOf<Product>()
    private var filteredList = mutableListOf<Product>()
    private lateinit var adapter: ProductAdapter
    private var snapshotListener: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProductAdapter(filteredList, 
            onEdit = { product -> showProductDialog(product) },
            onDelete = { product -> deleteProduct(product) }
        )
        binding.rvProducts.adapter = adapter

        binding.fabAddProduct.setOnClickListener {
            showProductDialog()
        }

        setupSearch()
        fetchProducts()
    }

    private fun setupSearch() {
        binding.etSearchProducts.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterProducts(query: String) {
        val lowercaseQuery = query.lowercase()
        filteredList.clear()
        if (lowercaseQuery.isEmpty()) {
            filteredList.addAll(productList)
        } else {
            for (product in productList) {
                if (product.name.lowercase().contains(lowercaseQuery) || 
                    product.category.lowercase().contains(lowercaseQuery)) {
                    filteredList.add(product)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun fetchProducts() {
        snapshotListener = db.collection("products").addSnapshotListener { snapshot, e ->
            if (_binding == null) return@addSnapshotListener
            if (e != null) {
                context?.let {
                    Toast.makeText(it, getString(R.string.error_products_load), Toast.LENGTH_SHORT).show()
                }
                return@addSnapshotListener
            }
            if (snapshot != null) {
                productList.clear()
                for (doc in snapshot.documents) {
                    val product = doc.toObject(Product::class.java)
                    product?.let {
                        productList.add(it.copy(id = doc.id))
                    }
                }
                filterProducts(binding.etSearchProducts.text.toString())
            }
        }
    }

    private fun showProductDialog(product: Product? = null) {
        val dialogBinding = DialogEditProductBinding.inflate(LayoutInflater.from(requireContext()))

        // Setup Category Selector in Dialog
        db.collection("products").get().addOnSuccessListener { snapshot ->
            val categories = snapshot.documents
                .mapNotNull { it.getString("category") }
                .filter { it.isNotEmpty() }
                .distinct()
                .sorted()
            val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
            (dialogBinding.etProductCategory as? AutoCompleteTextView)?.setAdapter(categoryAdapter)
        }

        product?.let {
            dialogBinding.etProductName.setText(it.name)
            dialogBinding.etProductPrice.setText(it.price.toString())
            dialogBinding.etProductStock.setText(it.stock.toString())
            dialogBinding.etProductCategory.setText(it.category)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (product == null) getString(R.string.add_product_title) else getString(R.string.edit_product_title))
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.save), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.etProductName.text.toString().trim()
                val price = dialogBinding.etProductPrice.text.toString().toDoubleOrNull() ?: 0.0
                val stock = dialogBinding.etProductStock.text.toString().toIntOrNull() ?: 0
                val category = dialogBinding.etProductCategory.text.toString().trim()

                if (name.isNotEmpty() && category.isNotEmpty()) {
                    val productId = product?.id ?: db.collection("products").document().id
                    val updatedProduct = (product ?: Product()).copy(
                        id = productId,
                        name = name,
                        price = price,
                        stock = stock,
                        category = category
                    )
                    
                    db.collection("products").document(productId).set(updatedProduct)
                        .addOnSuccessListener { 
                            Toast.makeText(context, if (product == null) getString(R.string.success_product_created) else getString(R.string.success_product_updated), Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, getString(R.string.error_save, it.message), Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.error_required_fields), Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun deleteProduct(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_product_title))
            .setMessage(getString(R.string.delete_product_confirm, product.name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                db.collection("products").document(product.id).delete()
                    .addOnSuccessListener { Toast.makeText(context, getString(R.string.success_product_deleted), Toast.LENGTH_SHORT).show() }
            }
            .setNegativeButton(getString(R.string.cancel), null)
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
                val context = holder.itemView.context
                tvProductPrice.text = context.getString(R.string.price_format, product.price)
                tvProductStock.text = context.getString(R.string.stock_category_format, product.stock, product.category)
                
                btnEditProduct.setOnClickListener { onEdit(product) }
                btnDeleteProduct.setOnClickListener { onDelete(product) }
            }
        }

        override fun getItemCount() = products.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snapshotListener?.remove()
        _binding = null
    }
}
