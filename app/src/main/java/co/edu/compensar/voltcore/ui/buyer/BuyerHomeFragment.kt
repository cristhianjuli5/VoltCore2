package co.edu.compensar.voltcore.ui.buyer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.data.Product
import co.edu.compensar.voltcore.data.User
import co.edu.compensar.voltcore.databinding.FragmentBuyerHomeBinding
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BuyerHomeFragment : Fragment() {
    private var _binding: FragmentBuyerHomeBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }
    
    private val allProducts = mutableListOf<Product>()
    private val featuredProducts = mutableListOf<Product>()
    private lateinit var adapter: CatalogFragment.CatalogAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBuyerHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUserGreeting()
        setupRecyclerView()
        setupSearch()
        fetchData()
    }

    private fun setupUserGreeting() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            db.collection("users").document(uid).get()
                .addOnSuccessListener { snapshot ->
                    if (!isAdded) return@addOnSuccessListener
                    val user = snapshot.toObject(User::class.java)
                    user?.let {
                        binding.tvWelcomeUser.text = "Hola, ${it.name} 👋"
                    }
                }
        }
    }

    private fun setupRecyclerView() {
        adapter = CatalogFragment.CatalogAdapter(featuredProducts) { product ->
            val bundle = Bundle().apply {
                putString("productId", product.id)
            }
            findNavController().navigate(R.id.productDetailFragment, bundle)
        }
        binding.rvFeatured.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterFeatured(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterFeatured(query: String) {
        val filtered = if (query.isEmpty()) {
            allProducts.take(10)
        } else {
            allProducts.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.category.contains(query, ignoreCase = true) 
            }
        }
        featuredProducts.clear()
        featuredProducts.addAll(filtered)
        adapter.notifyDataSetChanged()
    }

    private fun fetchData() {
        db.collection("products")
            .whereEqualTo("status", "AVAILABLE")
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null) return@addSnapshotListener
                
                val products = snapshot.toObjects(Product::class.java).filter { it.stock > 0 }
                allProducts.clear()
                allProducts.addAll(products)
                
                // Actualizar categorías dinámicamente
                updateCategories(products)
                
                // Mostrar algunos productos como destacados
                filterFeatured(binding.etSearch.text.toString())
            }
    }

    private fun updateCategories(products: List<Product>) {
        val categories = products.map { it.category }.distinct().filter { it.isNotEmpty() }.sorted()
        
        binding.chipGroupCategories.removeAllViews()
        
        for (category in categories) {
            val chip = Chip(requireContext()).apply {
                text = category
                isCheckable = true
                setChipBackgroundColorResource(R.color.glass_surface)
                setTextColor(resources.getColor(R.color.white, null))
                setOnClickListener {
                    // Al seleccionar una categoría, vamos al catálogo filtrado
                    val bundle = Bundle().apply {
                        putString("category", category)
                    }
                    findNavController().navigate(R.id.catalogFragment, bundle)
                }
            }
            binding.chipGroupCategories.addView(chip)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
