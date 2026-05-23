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

        // 1. Validate Firebase config before anything else
        val firebaseReady = validateFirebaseConfig()
        if (!firebaseReady) return

        // 2. Delay then check session
        Handler(Looper.getMainLooper()).postDelayed({
            checkSession()
        }, 2000)
    }

    /**
     * Validates that Firebase is correctly initialized and that
     * FirebaseAuth is reachable. Returns false if a critical error
     * is detected so the splash can stop early.
     */
    private fun validateFirebaseConfig(): Boolean {
        return try {
            val app = FirebaseApp.getInstance()
            val options = app.options

            Log.d(TAG, "✅ Firebase initialized → name: ${app.name}")
            Log.d(TAG, "   Project ID       : ${options.projectId}")
            Log.d(TAG, "   Application ID   : ${options.applicationId}")
            Log.d(TAG, "   API Key present  : ${options.apiKey.isNotBlank()}")

            // Warm-up FirebaseAuth so any configuration error surfaces here,
            // not silently inside checkSession().
            val authInstance = FirebaseAuth.getInstance()
            Log.d(TAG, "✅ FirebaseAuth instance ready")

            // Log current session state for diagnostics
            val currentUser = authInstance.currentUser
            if (currentUser != null) {
                Log.d(TAG, "👤 Existing session → uid: ${currentUser.uid}, email: ${currentUser.email}")
            } else {
                Log.d(TAG, "🔑 No active session, will navigate to login")
            }

            true

        } catch (e: IllegalStateException) {
            // FirebaseApp.getInstance() throws this when google-services.json
            // is missing or the Gradle plugin did not apply it.
            Log.e(TAG, "❌ Firebase NOT initialized — google-services.json may be missing or misplaced", e)
            Log.e(TAG, "   ➜ Make sure google-services.json is inside the /app folder (not the project root)")
            showFatalError(getString(R.string.firebase_not_init_error))
            false

        } catch (e: Exception) {
            // Any other exception here likely maps to CONFIGURATION_NOT_FOUND,
            // which means the Authentication provider is disabled in the Console.
            Log.e(TAG, "❌ Firebase config error — possible CONFIGURATION_NOT_FOUND", e)
            Log.e(TAG, "   ➜ Go to Firebase Console → Authentication → Sign-in method → enable Email/Password")
            Log.e(TAG, "   ➜ Also confirm a Support email is set in Project Settings → General")
            showFatalError(getString(R.string.firebase_config_error))
            false
        }
    }

    private fun checkSession() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "Session found → fetching user data for uid: ${currentUser.uid}")
            fetchUserData(currentUser.uid)
        } else {
            Log.d(TAG, "No session → navigating to login")
            navigateSafely { findNavController().navigate(R.id.action_splash_to_login) }
        }
    }

    private fun fetchUserData(uid: String) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snapshot ->
                if (!isAdded) return@addOnSuccessListener

                val user = snapshot.toObject(User::class.java)
                if (user != null) {
                    Log.d(TAG, "✅ User fetched → role: ${user.role}")
                    navigateToDashboard(user.role)
                } else {
                    Log.w(TAG, "⚠️ User document not found in Firestore → signing out")
                    auth.signOut()
                    navigateSafely { findNavController().navigate(R.id.action_splash_to_login) }
                }
            }
            .addOnFailureListener { e ->
                if (!isAdded) return@addOnFailureListener
                Log.e(TAG, "❌ Failed to fetch user data", e)
                navigateSafely { findNavController().navigate(R.id.action_splash_to_login) }
            }
    }

    private fun navigateToDashboard(role: UserRole) {
        navigateSafely {
            when (role) {
                UserRole.ADMIN  -> findNavController().navigate(R.id.action_splash_to_adminDashboard)
                UserRole.VENDOR -> findNavController().navigate(R.id.action_splash_to_vendorDashboard)
                UserRole.BUYER  -> findNavController().navigate(R.id.action_splash_to_buyerHome)
            }
        }
    }

    /**
     * Guards every NavController call: navigation is only executed when
     * the fragment is still attached and the view is alive.
     */
    private fun navigateSafely(action: () -> Unit) {
        if (isAdded && _binding != null) {
            action()
        } else {
            Log.w(TAG, "⚠️ navigateSafely: fragment detached, navigation skipped")
        }
    }

    private fun showFatalError(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}