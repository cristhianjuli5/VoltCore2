package co.edu.compensar.voltcore.ui.vendor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import co.edu.compensar.voltcore.databinding.FragmentVendorProductsBinding

class VendorProductsFragment : Fragment() {
    private var _binding: FragmentVendorProductsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVendorProductsBinding.inflate(inflater, container, false)
        
        binding.fabAddProduct.setOnClickListener {
            Toast.makeText(context, "Abriendo formulario de creación de producto (CRUD)", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
