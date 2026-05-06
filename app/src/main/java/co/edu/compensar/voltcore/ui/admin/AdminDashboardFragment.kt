package co.edu.compensar.voltcore.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.databinding.FragmentAdminDashboardBinding

class AdminDashboardFragment : Fragment() {
    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnManageUsers.setOnClickListener {
            findNavController().navigate(R.id.adminUsersFragment)
        }

        binding.btnManageProducts.setOnClickListener {
            findNavController().navigate(R.id.adminModerationFragment)
        }

        binding.btnViewReports.setOnClickListener {
            findNavController().navigate(R.id.adminReportsFragment)
        }

        binding.btnModeration.setOnClickListener {
            findNavController().navigate(R.id.adminModerationFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
