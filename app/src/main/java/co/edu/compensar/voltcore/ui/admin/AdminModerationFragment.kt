package co.edu.compensar.voltcore.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.compensar.voltcore.data.Product
import co.edu.compensar.voltcore.databinding.FragmentAdminModerationBinding
import co.edu.compensar.voltcore.databinding.ItemModerationPendingBinding
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class AdminModerationFragment : Fragment() {
    private var _binding: FragmentAdminModerationBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val pendingProducts = mutableListOf<Product>()
    private lateinit var adapter: ModerationAdapter
    private var snapshotListener: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminModerationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ModerationAdapter(pendingProducts) { product, isApproved ->
            handleModeration(product, isApproved)
        }
        
        binding.rvModeration.layoutManager = LinearLayoutManager(context)
        binding.rvModeration.adapter = adapter

        fetchPendingProducts()
    }

    private fun fetchPendingProducts() {
        snapshotListener = db.collection("products").addSnapshotListener { snapshot, e ->
            if (_binding == null) return@addSnapshotListener
            if (e != null) return@addSnapshotListener
            if (snapshot != null) {
                pendingProducts.clear()
                for (doc in snapshot.documents) {
                    val product = doc.toObject(Product::class.java)?.copy(id = doc.id)
                    if (product != null) pendingProducts.add(product)
                }
                adapter.notifyDataSetChanged()
                
                binding.tvEmptyModeration.visibility = if (pendingProducts.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    private fun handleModeration(product: Product, isApproved: Boolean) {
        val action = if (isApproved) "aprobado" else "rechazado"
        Toast.makeText(context, "Producto ${product.name} $action", Toast.LENGTH_SHORT).show()
    }

    class ModerationAdapter(
        private val products: List<Product>,
        private val onAction: (Product, Boolean) -> Unit
    ) : RecyclerView.Adapter<ModerationAdapter.ViewHolder>() {

        class ViewHolder(val binding: ItemModerationPendingBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemModerationPendingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val product = products[position]
            holder.binding.tvProductName.text = product.name
            holder.binding.tvVendorName.text = "Categoría: ${product.category}"
            
            Glide.with(holder.binding.ivProduct.context)
                .load(product.imageUrl)
                .placeholder(co.edu.compensar.voltcore.R.drawable.ic_voltcore_logo)
                .into(holder.binding.ivProduct)

            holder.binding.btnApprove.setOnClickListener { onAction(product, true) }
            holder.binding.btnReject.setOnClickListener { onAction(product, false) }
        }

        override fun getItemCount() = products.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snapshotListener?.remove()
        _binding = null
    }
}
