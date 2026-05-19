package co.edu.compensar.voltcore.ui.vendor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.data.User
import co.edu.compensar.voltcore.databinding.FragmentVendorProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.regex.Pattern

class VendorProfileFragment : Fragment() {
    private var _binding: FragmentVendorProfileBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private val nitPattern = Pattern.compile("^[0-9]{7,10}-[0-9]$")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVendorProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fetchVendorData()

        binding.btnUploadRUT.setOnClickListener {
            Toast.makeText(context, "Abriendo selector de archivos para RUT", Toast.LENGTH_SHORT).show()
        }

        binding.btnSaveProfile.setOnClickListener {
            validateAndSave()
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

    private fun fetchVendorData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.toObject(User::class.java)
                user?.let {
                    binding.etRazonSocial.setText(it.name)
                    // NIT no está en el modelo base User, pero podrías extenderlo o usar campos extras
                }
            }
    }

    private fun validateAndSave() {
        val nit = binding.etNIT.text.toString()
        val razonSocial = binding.etRazonSocial.text.toString().trim()

        if (razonSocial.isEmpty()) {
            binding.tilRazonSocial.error = "La razón social es obligatoria"
            return
        }
        binding.tilRazonSocial.error = null

        if (nit.isNotEmpty() && !nitPattern.matcher(nit).matches()) {
            binding.tilNIT.error = "Formato de NIT inválido (ej: 123456789-0)"
            return
        }
        binding.tilNIT.error = null

        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).update("name", razonSocial)
            .addOnSuccessListener {
                Toast.makeText(context, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
