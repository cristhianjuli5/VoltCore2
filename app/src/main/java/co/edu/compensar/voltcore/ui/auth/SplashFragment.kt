package co.edu.compensar.voltcore.ui.auth

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.data.User
import co.edu.compensar.voltcore.data.UserRole
import co.edu.compensar.voltcore.databinding.FragmentSplashBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashFragment : Fragment() {

    companion object {
        private const val TAG = "SplashFragment"
    }

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        validateFirebaseConfig()

        Handler(Looper.getMainLooper()).postDelayed({
            checkSession()
        }, 2000)
    }

    private fun validateFirebaseConfig() {
        try {
            val app = FirebaseApp.getInstance()
            Log.d(TAG, "Firebase initialized: ${app.name}")
            Log.d(TAG, "Project ID: ${app.options.projectId}")
            Log.d(TAG, "Application ID: ${app.options.applicationId}")
        } catch (e: Exception) {
            Log.e(TAG, "Firebase initialization failed", e)
            Toast.makeText(context, "Error de configuración de Firebase", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkSession() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            fetchUserData(currentUser.uid)
        } else {
            findNavController().navigate(R.id.action_splash_to_login)
        }
    }

    private fun fetchUserData(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snapshot ->
                if (isAdded) {
                    val user = snapshot.toObject(User::class.java)
                    if (user != null) {
                        navigateToDashboard(user.role)
                    } else {
                        auth.signOut()
                        findNavController().navigate(R.id.action_splash_to_login)
                    }
                }
            }
            .addOnFailureListener {
                if (isAdded) {
                    findNavController().navigate(R.id.action_splash_to_login)
                }
            }
    }

    private fun navigateToDashboard(role: UserRole) {
        when (role) {
            UserRole.ADMIN -> findNavController().navigate(R.id.action_splash_to_adminDashboard)
            UserRole.VENDOR -> findNavController().navigate(R.id.action_splash_to_vendorDashboard)
            UserRole.BUYER -> findNavController().navigate(R.id.action_splash_to_buyerHome)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
