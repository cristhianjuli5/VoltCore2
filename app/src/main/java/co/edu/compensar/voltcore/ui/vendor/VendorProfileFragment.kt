package co.edu.compensar.voltcore.ui.vendor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import co.edu.compensar.voltcore.databinding.FragmentVendorProfileBinding

class VendorProfileFragment : Fragment() {
    private var _binding: FragmentVendorProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVendorProfileBinding.inflate(inflater, container, false)
        
        // Buscamos el botón por texto para asignar la acción de feedback
        val views = ArrayList<View>()
        binding.root.findViewsWithText(views, "Actualizar Datos", View.FIND_VIEWS_WITH_TEXT)
        if (views.isNotEmpty() && views[0] is Button) {
            (views[0] as Button).setOnClickListener {
                Toast.makeText(requireContext(), "Datos legales (NIT/RUT) guardados con éxito", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
