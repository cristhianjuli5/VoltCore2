package co.edu.compensar.voltcore.ui.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.data.User
import co.edu.compensar.voltcore.data.UserRole
import co.edu.compensar.voltcore.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.Executor

class LoginFragment : Fragment() {

    companion object {
        private const val PREFS_NAME = "secure_prefs"
        private const val KEY_EMAIL = "stored_email"
        private const val KEY_PASSWORD = "stored_password"
        private const val TAG = "LoginFragment"
    }

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkBiometricSupport()
        setupBiometric()

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Por favor ingresa correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            handleLogin(email, password)
        }

        binding.btnBiometric.setOnClickListener {
            if (hasStoredCredentials()) {
                biometricPrompt.authenticate(promptInfo)
            } else {
                Toast.makeText(requireContext(), "Inicia sesión con contraseña primero para habilitar la biometría", Toast.LENGTH_LONG).show()
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_recovery)
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
    }

    private fun handleLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                saveCredentials(email, password)
                val uid = result.user?.uid ?: return@addOnSuccessListener
                fetchUserData(uid)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchUserData(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snapshot ->
                val user = snapshot.toObject(User::class.java)
                if (user != null) {
                    navigateToDashboard(user.role)
                } else {
                    Toast.makeText(context, "Error: No se encontró el perfil", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al obtener datos: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToDashboard(role: UserRole) {
        when (role) {
            UserRole.ADMIN -> findNavController().navigate(R.id.action_login_to_adminDashboard)
            UserRole.VENDOR -> findNavController().navigate(R.id.action_login_to_vendorDashboard)
            UserRole.BUYER -> findNavController().navigate(R.id.action_login_to_buyerHome)
        }
    }

    private fun setupBiometric() {
        executor = ContextCompat.getMainExecutor(requireContext())
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e(TAG, "Biometric error: $errorCode - $errString")
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val credentials = getStoredCredentials()
                    if (credentials != null) {
                        handleLogin(credentials.first, credentials.second)
                    } else {
                        Toast.makeText(requireContext(), "Error al recuperar credenciales", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(requireContext(), "Autenticación fallida", Toast.LENGTH_SHORT).show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticación Biométrica")
            .setSubtitle("Ingresa con tu huella o rostro")
            .setNegativeButtonText("Cancelar")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()
    }

    private fun checkBiometricSupport() {
        val biometricManager = BiometricManager.from(requireContext())
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                binding.btnBiometric.visibility = View.VISIBLE
            }
            else -> {
                binding.btnBiometric.visibility = View.GONE
            }
        }
    }

    private fun getEncryptedPrefs() = EncryptedSharedPreferences.create(
        requireContext(),
        PREFS_NAME,
        MasterKey.Builder(requireContext()).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun saveCredentials(email: String, password: String) {
        getEncryptedPrefs().edit().apply {
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
            apply()
        }
    }

    private fun getStoredCredentials(): Pair<String, String>? {
        val prefs = getEncryptedPrefs()
        val email = prefs.getString(KEY_EMAIL, null)
        val password = prefs.getString(KEY_PASSWORD, null)
        return if (email != null && password != null) email to password else null
    }

    private fun hasStoredCredentials(): Boolean {
        val prefs = getEncryptedPrefs()
        return prefs.contains(KEY_EMAIL) && prefs.contains(KEY_PASSWORD)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
