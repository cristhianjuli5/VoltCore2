package co.edu.compensar.voltcore.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.edu.compensar.voltcore.databinding.FragmentRecoveryBinding
import com.google.firebase.auth.FirebaseAuth

class RecoveryFragment : Fragment() {

    private var _binding: FragmentRecoveryBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecoveryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSendRecovery.setOnClickListener {
            val email = binding.etRecoveryEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(context, "Por favor, ingresa tu correo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendRecoveryEmail(email)
        }
    }

    private fun sendRecoveryEmail(email: String) {
        binding.btnSendRecovery.isEnabled = false
        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(context, "Enlace enviado a $email. Revisa tu bandeja de entrada.", Toast.LENGTH_LONG).show()
                findNavController().navigateUp()
            }
            .addOnFailureListener { e ->
                binding.btnSendRecovery.isEnabled = true
                val errorMsg = when {
                    e.message?.contains("CONFIGURATION_NOT_FOUND") == true -> 
                        "Error: Falta configurar el 'Correo de soporte' en la consola de Firebase."
                    e.message?.contains("USER_NOT_FOUND") == true -> 
                        "Error: No existe ninguna cuenta con este correo."
                    else -> "Error: ${e.localizedMessage}"
                }
                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
