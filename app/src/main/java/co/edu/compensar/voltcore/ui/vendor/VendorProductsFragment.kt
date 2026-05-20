package co.edu.compensar.voltcore.ui.vendor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.data.Product
import co.edu.compensar.voltcore.databinding.FragmentVendorProductsBinding
import co.edu.compensar.voltcore.databinding.ItemVendorProductBinding
import co.edu.compensar.voltcore.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VendorProductsFragment : Fragment() {
    private var _binding: FragmentVendorProductsBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val productList = mutableListOf<Product>()
    private lateinit var adapter: VendorProductAdapter
    private var snapshotListener: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVendorProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = VendorProductAdapter(
            products = productList,
            onDelete = { product -> showDeleteDialog(product) },
            onEdit = { product ->
                val bundle = Bundle().apply {
                    putString("productId", product.id)
                }
                findNavController().navigate(R.id.action_vendorProducts_to_form, bundle)
            }
        )
        binding.rvVendorProducts.adapter = adapter

        binding.fabAddProduct.setOnClickListener {
            findNavController().navigate(R.id.action_vendorProducts_to_form)
        }

        fetchVendorProducts()
    }

    private fun fetchVendorProducts() {
        val vendorId = auth.currentUser?.uid ?: return
        snapshotListener = db.collection("products")
            .whereEqualTo("vendorId", vendorId)
            .addSnapshotListener { snapshot, e ->
                if (_binding == null) return@addSnapshotListener
                if (e != null) {
                    context?.let {
                        Toast.makeText(it, "Error al cargar productos", Toast.LENGTH_SHORT).show()
                    }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    productList.clear()
                    for (doc in snapshot.documents) {
                        val product = doc.toObject(Product::class.java)?.copy(id = doc.id)
                        if (product != null) {
                            productList.add(product)
                        }
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    private fun showDeleteDialog(product: Product) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Producto")
            .setMessage("¿Estás seguro de que deseas eliminar '${product.name}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                if (product.id.isEmpty()) return@setPositiveButton
                
                db.collection("products").document(product.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error al eliminar producto", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    class VendorProductAdapter(
        private val products: List<Product>,
        private val onDelete: (Product) -> Unit,
        private val onEdit: (Product) -> Unit
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
                chipStatus.text = "ACTIVO"
                
                ImageUtils.loadImage(
                    holder.itemView.context,
                    product.imageUrl,
                    ivProduct,
                    tvProductEmoji
                )
                
                btnEdit.setOnClickListener { onEdit(product) }
                btnArchive.setOnClickListener { onDelete(product) }
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
