package co.edu.compensar.voltcore.ui.buyer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.data.CartManager
import co.edu.compensar.voltcore.data.Product
import co.edu.compensar.voltcore.databinding.FragmentCatalogBinding
import co.edu.compensar.voltcore.databinding.ItemProductCardBinding
import co.edu.compensar.voltcore.utils.ImageUtils
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore

class CatalogFragment : Fragment() {
    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val allProducts = mutableListOf<Product>()
    private val displayList = mutableListOf<Product>()
    private lateinit var adapter: CatalogAdapter
    private var snapshotListener: com.google.firebase.firestore.ListenerRegistration? = null
    
    private var selectedCategory: String? = null
    private var searchQuery: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recuperar categoría inicial si viene de Home
        selectedCategory = arguments?.getString("category")
        if (selectedCategory != null) {
            binding.chipAll.isChecked = false
        }

        adapter = CatalogAdapter(displayList) { product ->
            val bundle = Bundle().apply {
                putString("productId", product.id)
            }
            findNavController().navigate(R.id.productDetailFragment, bundle)
        }
        
        binding.rvCatalog.layoutManager = GridLayoutManager(context, 2)
        binding.rvCatalog.adapter = adapter

        setupSearch()
        setupCategoryFilters()
        fetchProducts()
    }

    private fun setupSearch() {
        binding.etSearchCatalog.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s.toString()
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupCategoryFilters() {
        binding.chipAll.setOnClickListener {
            selectedCategory = null
            applyFilters()
        }
    }

    private fun fetchProducts() {
        snapshotListener = db.collection("products")
            .whereEqualTo("status", "AVAILABLE")
            .addSnapshotListener { snapshot, e ->
                if (_binding == null) return@addSnapshotListener
                if (e != null) {
                    android.util.Log.e("CatalogFragment", "Error Firestore: ${e.message}")
                    context?.let {
                        Toast.makeText(it, getString(R.string.error_catalog_load), Toast.LENGTH_SHORT).show()
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    allProducts.clear()
                    val products = snapshot.toObjects(Product::class.java).filter { it.stock > 0 }
                    allProducts.addAll(products)
                    
                    updateCategoryChips(products)
                    applyFilters()
                }
            }
    }

    private fun updateCategoryChips(products: List<Product>) {
        val categories = products.map { it.category }.distinct().filter { it.isNotEmpty() }.sorted()
        
        // Mantener el chip "Todos"
        val count = binding.chipGroupFilters.childCount
        if (count > 1) {
            binding.chipGroupFilters.removeViews(1, count - 1)
        }

        for (category in categories) {
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                setChipBackgroundColorResource(R.color.glass_surface)
                setTextColor(resources.getColor(R.color.volt_text_primary, null))
                
                // Marcar si es la categoría seleccionada inicialmente
                if (category == selectedCategory) {
                    isChecked = true
                }

                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedCategory = category
                        binding.chipAll.isChecked = false
                    } else if (selectedCategory == category) {
                        selectedCategory = null
                    }
                    applyFilters()
                }
            }
            binding.chipGroupFilters.addView(chip)
        }
    }

    private fun applyFilters() {
        displayList.clear()
        val filtered = allProducts.filter { product ->
            val matchesCategory = selectedCategory == null || product.category == selectedCategory
            val matchesSearch = searchQuery.isEmpty() || 
                    product.name.contains(searchQuery, ignoreCase = true) ||
                    product.description.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
        displayList.addAll(filtered)
        adapter.notifyDataSetChanged()
    }

    class CatalogAdapter(
        private val products: List<Product>,
        private val onItemClick: (Product) -> Unit
    ) : RecyclerView.Adapter<CatalogAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemProductCardBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemProductCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val product = products[position]
            holder.binding.tvProductName.text = product.name
            holder.binding.tvProductPrice.text = holder.itemView.context.getString(R.string.price_format, product.price)
            
            ImageUtils.loadImage(
                holder.itemView.context,
                product.imageUrl,
                holder.binding.ivProduct,
                holder.binding.tvProductEmoji
            )
            
            holder.binding.root.setOnClickListener { onItemClick(product) }

            holder.binding.btnAddToCart.setOnClickListener {
                CartManager.addItem(product)
                Toast.makeText(holder.itemView.context, holder.itemView.context.getString(R.string.product_added_msg, product.name), Toast.LENGTH_SHORT).show()
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
