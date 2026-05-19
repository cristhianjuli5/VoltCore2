package co.edu.compensar.voltcore.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.data.Order
import co.edu.compensar.voltcore.databinding.FragmentAdminDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class AdminDashboardFragment : Fragment() {
    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchRealStats()

        binding.btnManageUsers.setOnClickListener {
            findNavController().navigate(R.id.adminUsersFragment)
        }

        binding.btnManageProducts.setOnClickListener {
            findNavController().navigate(R.id.adminProductsFragment)
        }

        binding.btnViewReports.setOnClickListener {
            findNavController().navigate(R.id.adminReportsFragment)
        }

        binding.btnModeration.setOnClickListener {
            findNavController().navigate(R.id.adminModerationFragment)
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.loginFragment, null,
                androidx.navigation.NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()
            )
        }
    }

    private fun fetchRealStats() {
        // Usuarios Reales
        db.collection("users").get().addOnSuccessListener { snapshot ->
            if (_binding == null) return@addOnSuccessListener
            binding.tvUserCount.text = snapshot.size().toString()
        }

        // Ventas Reales (Suma de todos los pedidos)
        db.collection("orders").get().addOnSuccessListener { snapshot ->
            if (_binding == null) return@addOnSuccessListener
            var totalSales = 0.0
            for (doc in snapshot.documents) {
                val order = doc.toObject(Order::class.java)
                if (order != null) {
                    totalSales += order.total
                }
            }
            binding.tvTotalSales.text = String.format(Locale.getDefault(), "$ %,.0f", totalSales)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
