package co.edu.compensar.voltcore.ui.vendor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.databinding.FragmentVendorDashboardBinding

class VendorDashboardFragment : Fragment() {
    private var _binding: FragmentVendorDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVendorDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
