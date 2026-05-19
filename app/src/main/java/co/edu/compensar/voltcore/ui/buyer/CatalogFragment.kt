package co.edu.compensar.voltcore.ui.buyer

import android.os.Bundle
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
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class CatalogFragment : Fragment() {
    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val productList = mutableListOf<Product>()
    private lateinit var adapter: CatalogAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CatalogAdapter(productList) { product ->
            val bundle = Bundle().apply {
                putString("productId", product.id)
            }
            findNavController().navigate(R.id.productDetailFragment, bundle)
        }
        
        binding.rvCatalog.layoutManager = GridLayoutManager(context, 2)
        binding.rvCatalog.adapter = adapter

        fetchProducts()
    }

    private fun fetchProducts() {
        db.collection("products")
            .whereEqualTo("status", "AVAILABLE")
            .whereGreaterThan("stock", 0)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Toast.makeText(context, "Error al cargar catálogo", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    productList.clear()
                    productList.addAll(snapshot.toObjects(Product::class.java))
                    adapter.notifyDataSetChanged()
                }
            }
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
            holder.binding.tvProductPrice.text = String.format("$ %,.0f", product.price)
            
            if (product.imageUrl.length <= 4) {
                holder.binding.ivProduct.visibility = View.GONE
                holder.binding.tvProductEmoji.visibility = View.VISIBLE
                holder.binding.tvProductEmoji.text = product.imageUrl
            } else {
                holder.binding.ivProduct.visibility = View.VISIBLE
                holder.binding.tvProductEmoji.visibility = View.GONE
                Glide.with(holder.binding.ivProduct.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.ic_voltcore_logo)
                    .into(holder.binding.ivProduct)
            }
            
            holder.binding.root.setOnClickListener { onItemClick(product) }

            holder.binding.btnAddToCart.setOnClickListener {
                CartManager.addItem(product)
                Toast.makeText(holder.itemView.context, "${product.name} añadido", Toast.LENGTH_SHORT).show()
            }
        }

        override fun getItemCount() = products.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
