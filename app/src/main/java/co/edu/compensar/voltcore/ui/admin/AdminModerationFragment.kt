package co.edu.compensar.voltcore.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import co.edu.compensar.voltcore.databinding.FragmentAdminModerationBinding

class AdminModerationFragment : Fragment() {
    private var _binding: FragmentAdminModerationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminModerationBinding.inflate(inflater, container, false)
        
        // Simular acciones de moderación
        binding.root.findViewWithTag<View>(null)?.let { } // Dummy
        
        // En el layout pusimos ImageButtons, les damos funcionalidad de feedback
        val views = ArrayList<View>()
        binding.root.findViewsWithText(views, "Aprobar", View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION)
        if (views.isNotEmpty()) views[0].setOnClickListener {
            Toast.makeText(context, "Producto aprobado para el catálogo", Toast.LENGTH_SHORT).show()
        }

        val views2 = ArrayList<View>()
        binding.root.findViewsWithText(views2, "Rechazar", View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION)
        if (views2.isNotEmpty()) views2[0].setOnClickListener {
            Toast.makeText(context, "Producto rechazado", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
