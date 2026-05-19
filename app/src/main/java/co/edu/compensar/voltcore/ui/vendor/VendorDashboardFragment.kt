package co.edu.compensar.voltcore.ui.vendor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.databinding.FragmentVendorDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VendorDashboardFragment : Fragment() {
    private var _binding: FragmentVendorDashboardBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVendorDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupNavigation()
        fetchStats()

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.loginFragment, null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
            )
        }
    }

    private fun setupNavigation() {
        binding.btnVendorProducts.setOnClickListener {
            findNavController().navigate(R.id.vendorProductsFragment)
        }

        binding.btnVendorOrders.setOnClickListener {
            findNavController().navigate(R.id.vendorOrdersFragment)
        }

        binding.btnVendorProfile.setOnClickListener {
            findNavController().navigate(R.id.vendorProfileFragment)
        }
    }

    private fun fetchStats() {
        val vendorId = auth.currentUser?.uid ?: return

        // Contar productos del vendedor
        db.collection("products")
            .whereEqualTo("vendorId", vendorId)
            .get()
            .addOnSuccessListener { snapshot ->
                binding.tvProductCount.text = snapshot.size().toString()
            }

        // Contar pedidos (por ahora todos, ya que el modelo de pedidos es global)
        db.collection("orders")
            .get()
            .addOnSuccessListener { snapshot ->
                binding.tvOrderCount.text = snapshot.size().toString()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
