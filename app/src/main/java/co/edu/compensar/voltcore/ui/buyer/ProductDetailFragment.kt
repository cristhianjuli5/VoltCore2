package co.edu.compensar.voltcore.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.databinding.FragmentProductDetailBinding

import com.google.firebase.firestore.FirebaseFirestore
import co.edu.compensar.voltcore.data.Product
import co.edu.compensar.voltcore.data.CartManager
import com.bumptech.glide.Glide

class ProductDetailFragment : Fragment() {
    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val db by lazy { FirebaseFirestore.getInstance() }
    private var product: Product? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productId = arguments?.getString("productId")
        if (productId != null) {
            fetchProductDetails(productId)
        }

        binding.btnAddToCart.setOnClickListener {
            product?.let {
                CartManager.addItem(it)
                Toast.makeText(context, "${it.name} añadido al carrito", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnBuyNow.setOnClickListener {
            product?.let {
                CartManager.clear() // Limpiar para compra única rápida
                CartManager.addItem(it)
                findNavController().navigate(R.id.checkoutFragment)
            }
        }
    }

    private fun fetchProductDetails(productId: String) {
        db.collection("products").document(productId).get()
            .addOnSuccessListener { document ->
                product = document.toObject(Product::class.java)
                product?.let { displayProduct(it) }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar detalles", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayProduct(product: Product) {
        binding.tvDetailName.text = product.name
        binding.tvDetailDescription.text = product.description
        binding.tvDetailPrice.text = "$ %,.0f".format(product.price)
        
        if (product.imageUrl.length <= 4) {
            binding.ivDetailProduct.visibility = View.GONE
            binding.tvDetailEmoji.visibility = View.VISIBLE
            binding.tvDetailEmoji.text = product.imageUrl
        } else {
            binding.ivDetailProduct.visibility = View.VISIBLE
            binding.tvDetailEmoji.visibility = View.GONE
            Glide.with(this)
                .load(product.imageUrl)
                .into(binding.ivDetailProduct)
        }
        
        binding.tvTechnicalSheet.text = "Stock: ${product.stock}\nCategoría: ${product.category}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
