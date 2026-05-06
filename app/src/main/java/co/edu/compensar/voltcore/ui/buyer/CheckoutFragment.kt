package co.edu.compensar.voltcore.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.databinding.FragmentCheckoutBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CheckoutFragment : Fragment() {
    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirmPayment.setOnClickListener {
            simulatePayment()
        }
    }

    private fun simulatePayment() {
        binding.btnConfirmPayment.isEnabled = false
        binding.pbPayment.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            delay(3000) // Simular espera de pasarela
            binding.pbPayment.visibility = View.GONE
            Toast.makeText(context, "¡PAGO EXITOSO! Tu pedido está en camino.", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.buyerHomeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
