package co.edu.compensar.voltcore.ui.buyer

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.data.Order
import co.edu.compensar.voltcore.data.CartManager
import co.edu.compensar.voltcore.data.User
import co.edu.compensar.voltcore.databinding.FragmentCheckoutBinding
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class CheckoutFragment : Fragment() {
    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val fusedLocationClient by lazy { LocationServices.getFusedLocationProviderClient(requireActivity()) }

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            getCurrentLocation()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    private var selectedPaymentMethod = "CARD"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()
        setupPaymentMethods()

        binding.btnConfirmPayment.setOnClickListener {
            processPayment()
        }

        binding.btnGetLocation.setOnClickListener {
            requestLocationPermissions()
        }

        requestLocationPermissions()
    }

    private fun setupPaymentMethods() {
        // Bancos PSE
        val banks = listOf("Bancolombia", "Banco de Bogotá", "Davivienda", "BBVA", "Scotiabank Colpatria", "Banco Falabella", "RappiPay", "Lulo Bank")
        val bankAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, banks)
        binding.actvBank.setAdapter(bankAdapter)

        // Tipos de Persona
        val personTypes = listOf("Persona Natural", "Persona Jurídica")
        val personAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, personTypes)
        binding.actvPersonType.setAdapter(personAdapter)

        // Tipos de Documento
        val docTypes = listOf("CC", "CE", "NIT", "PP")
        val docAdapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, docTypes)
        binding.actvDocType.setAdapter(docAdapter)

        binding.cgPaymentMethods.setOnCheckedStateChangeListener { _, checkedIds ->
            when (checkedIds.firstOrNull()) {
                R.id.chipCard -> {
                    selectedPaymentMethod = "CARD"
                    binding.cvCardDetails.visibility = View.VISIBLE
                    binding.cvWalletDetails.visibility = View.GONE
                    binding.cvCashDetails.visibility = View.GONE
                    binding.cvTransferDetails.visibility = View.GONE
                }
                R.id.chipWallet -> {
                    selectedPaymentMethod = "WALLET"
                    binding.cvCardDetails.visibility = View.GONE
                    binding.cvWalletDetails.visibility = View.VISIBLE
                    binding.cvCashDetails.visibility = View.GONE
                    binding.cvTransferDetails.visibility = View.GONE
                }
                R.id.chipCash -> {
                    selectedPaymentMethod = "CASH"
                    binding.cvCardDetails.visibility = View.GONE
                    binding.cvWalletDetails.visibility = View.GONE
                    binding.cvCashDetails.visibility = View.VISIBLE
                    binding.cvTransferDetails.visibility = View.GONE
                }
                R.id.chipTransfer -> {
                    selectedPaymentMethod = "TRANSFER"
                    binding.cvCardDetails.visibility = View.GONE
                    binding.cvWalletDetails.visibility = View.GONE
                    binding.cvCashDetails.visibility = View.GONE
                    binding.cvTransferDetails.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun loadUserData() {
        // ... (existing code remains same)
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { snapshot ->
            if (_binding == null) return@addOnSuccessListener
            val user = snapshot.toObject(User::class.java)
            user?.let {
                binding.etFullName.setText(it.name)
                binding.etShippingAddress.setText(it.address)
                binding.etReceiver.setText(it.name)
            }
        }
    }

    private fun requestLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        } else {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (_binding == null) return
        Toast.makeText(context, getString(R.string.fetching_location), Toast.LENGTH_SHORT).show()
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            val currentContext = context ?: return@addOnSuccessListener
            if (location != null && _binding != null) {
                try {
                    val geocoder = Geocoder(currentContext, Locale.getDefault())
                    
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                            viewLifecycleOwner.lifecycleScope.launch(kotlinx.coroutines.Dispatchers.Main) {
                                updateLocationUI(addresses)
                            }
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        updateLocationUI(addresses)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("Checkout", "Error geocoding", e)
                }
            } else {
                Toast.makeText(context, getString(R.string.location_not_found), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateLocationUI(addresses: List<android.location.Address>?) {
        if (_binding != null && !addresses.isNullOrEmpty()) {
            val address = addresses[0].getAddressLine(0)
            binding.etShippingAddress.setText(address)
            val city = "${addresses[0].locality ?: ""}, ${addresses[0].adminArea ?: ""}"
            binding.etCity.setText(city)
        }
    }

    private fun processPayment() {
        if (!validateFields()) return

        val address = binding.etShippingAddress.text.toString()
        val city = binding.etCity.text.toString()

        binding.btnConfirmPayment.isEnabled = false
        binding.pbPayment.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val transactionId = "VC-" + UUID.randomUUID().toString().take(8).uppercase()
                
                when (selectedPaymentMethod) {
                    "WALLET" -> {
                        Toast.makeText(context, "Notificación enviada a Nequi. Confirma en tu app...", Toast.LENGTH_LONG).show()
                        delay(4000) // Simulación de espera de aprobación PUSH
                    }
                    "TRANSFER" -> {
                        Toast.makeText(context, "Redirigiendo a portal bancario seguro...", Toast.LENGTH_SHORT).show()
                        delay(3000)
                        Toast.makeText(context, "Procesando código de seguridad (OTP)...", Toast.LENGTH_SHORT).show()
                        delay(2000)
                    }
                    else -> {
                        delay(2000) // Simulación estándar (Tarjeta/Efectivo)
                    }
                }
                
                saveOrder(address, city, transactionId)
            } catch (e: Exception) {
                if (_binding != null) {
                    binding.btnConfirmPayment.isEnabled = true
                    binding.pbPayment.visibility = View.GONE
                    Toast.makeText(context, "Error en el pago: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true
        
        // Validación de campos personales y de envío
        val commonFields = listOf(
            binding.tilFullName to binding.etFullName,
            binding.tilDocument to binding.etDocument,
            binding.tilPhone to binding.etPhone,
            binding.tilShippingAddress to binding.etShippingAddress,
            binding.tilCity to binding.etCity,
            binding.tilReceiver to binding.etReceiver
        )

        for ((layout, editText) in commonFields) {
            if (editText.text.toString().trim().isEmpty()) {
                layout.error = "Campo obligatorio"
                isValid = false
            } else {
                layout.error = null
            }
        }

        // Validación específica según método de pago
        when (selectedPaymentMethod) {
            "CARD" -> {
                if (binding.etCardNumber.text?.length ?: 0 < 16) {
                    binding.tilCardNumber.error = "Número incompleto"
                    isValid = false
                }
                if (binding.etCvv.text?.length ?: 0 < 3) {
                    binding.tilCvv.error = "CVV inválido"
                    isValid = false
                }
            }
            "WALLET" -> {
                if (binding.etWalletPhone.text.toString().trim().isEmpty()) {
                    binding.tilWalletPhone.error = "Número de celular obligatorio"
                    isValid = false
                }
            }
            "TRANSFER" -> {
                if (binding.actvBank.text.toString().isEmpty()) {
                    binding.tilBankName.error = "Selecciona un banco"
                    isValid = false
                }
                if (binding.actvPersonType.text.toString().isEmpty()) {
                    binding.tilPersonType.error = "Selecciona tipo de persona"
                    isValid = false
                }
                if (binding.etDocNumber.text.toString().isEmpty()) {
                    binding.tilDocNumber.error = "Número de documento obligatorio"
                    isValid = false
                }
            }
        }

        return isValid
    }

    private fun saveOrder(address: String, city: String, transactionId: String) {
        val buyerId = auth.currentUser?.uid ?: return
        val items = CartManager.getItems()
        if (items.isEmpty()) {
            Toast.makeText(context, "El carrito está vacío", Toast.LENGTH_SHORT).show()
            return
        }

        val paymentDetails = when(selectedPaymentMethod) {
            "CARD" -> "VISA-****" + binding.etCardNumber.text.toString().takeLast(4)
            "WALLET" -> "Nequi PUSH: " + binding.etWalletPhone.text.toString()
            "TRANSFER" -> "PSE " + binding.actvBank.text.toString() + " - " + binding.etDocNumber.text.toString()
            else -> "Convenio Efecty: " + transactionId.takeLast(6)
        }

        val order = Order(
            id = transactionId,
            buyerId = buyerId,
            items = items,
            total = CartManager.getTotalPrice(),
            status = if (selectedPaymentMethod == "CASH") "PENDING_CASH" else "PAID",
            address = "$address, $city",
            timestamp = Date(),
            paymentMethod = selectedPaymentMethod,
            paymentDetails = paymentDetails
        )

        val batch = db.batch()
        val orderRef = db.collection("orders").document(transactionId)
        batch.set(orderRef, order)

        items.forEach { item ->
            val productRef = db.collection("products").document(item.productId)
            batch.update(productRef, "stock", com.google.firebase.firestore.FieldValue.increment(-item.quantity.toLong()))
        }

        batch.commit()
            .addOnSuccessListener {
                if (_binding != null) {
                    binding.pbPayment.visibility = View.GONE
                    CartManager.clear()
                    Toast.makeText(context, "¡PAGO EXITOSO! Orden: $transactionId", Toast.LENGTH_LONG).show()
                    findNavController().navigate(R.id.action_checkout_to_buyerHome)
                }
            }
            .addOnFailureListener {
                if (_binding != null) {
                    binding.btnConfirmPayment.isEnabled = true
                    binding.pbPayment.visibility = View.GONE
                    Toast.makeText(context, "Error al guardar orden", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
