package co.edu.compensar.voltcore.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.data.User
import co.edu.compensar.voltcore.databinding.FragmentBuyerProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BuyerProfileFragment : Fragment() {
    private var _binding: FragmentBuyerProfileBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBuyerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchUserData()

        binding.btnSaveProfile.setOnClickListener {
            updateProfile()
        }

        binding.btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        binding.btnMyOrders.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_orders)
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

    private fun fetchUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snapshot ->
                if (_binding == null) return@addOnSuccessListener
                val user = snapshot.toObject(User::class.java)
                user?.let {
                    binding.tvProfileName.text = it.name
                    binding.tvProfileEmail.text = it.email
                    binding.etFullName.setText(it.name)
                    binding.etAddress.setText(it.address)
                }
            }
    }

    private fun updateProfile() {
        val uid = auth.currentUser?.uid ?: return
        val newName = binding.etFullName.text.toString().trim()
        val newAddress = binding.etAddress.text.toString().trim()

        if (newName.isEmpty()) {
            binding.tilFullName.error = "El nombre es obligatorio"
            return
        }
        binding.tilFullName.error = null

        val updates = mapOf(
            "name" to newName,
            "address" to newAddress
        )

        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                binding.tvProfileName.text = newName
                Toast.makeText(context, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showChangePasswordDialog() {
        val email = auth.currentUser?.email ?: return
        
        AlertDialog.Builder(requireContext())
            .setTitle("Cambiar Contraseña")
            .setMessage("Se enviará un correo a $email para que puedas restablecer tu contraseña de forma segura.")
            .setPositiveButton("Enviar Correo") { _, _ ->
                auth.sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Correo enviado. Revisa tu bandeja de entrada.", Toast.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
