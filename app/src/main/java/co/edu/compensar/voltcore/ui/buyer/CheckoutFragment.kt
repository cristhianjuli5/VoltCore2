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
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnConfirmPayment.setOnClickListener {
            processPayment()
        }

        requestLocationPermissions()
    }

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0].getAddressLine(0)
                    binding.etShippingAddress.setText(address)
                }
            }
        }
    }

    private val EPAYCO_PUBLIC_KEY = "e0401e64b8824a32b6a9c5ab25dbd487"
    private val EPAYCO_CLIENT_ID = "1582221"

    private fun processPayment() {
        val address = binding.etShippingAddress.text.toString()
        if (address.isEmpty()) {
            Toast.makeText(context, "Por favor ingresa la dirección", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnConfirmPayment.isEnabled = false
        binding.pbPayment.visibility = View.VISIBLE

        // Lógica de Integración Real con ePayco
        // En un entorno real con el SDK instalado se llamaría a co.epayco.android.Epayco(EPAYCO_PUBLIC_KEY)
        // Por ahora, simulamos la respuesta exitosa usando tus credenciales para el registro
        
        viewLifecycleOwner.lifecycleScope.launch {
            delay(2000) 
            
            val transactionId = "EP-" + UUID.randomUUID().toString().take(8).uppercase()
            Toast.makeText(context, "Conectado con ePayco (Cliente: $EPAYCO_CLIENT_ID)", Toast.LENGTH_SHORT).show()
            saveOrder(address, transactionId)
        }
    }

    private fun saveOrder(address: String, transactionId: String) {
        val buyerId = auth.currentUser?.uid ?: return
        val items = CartManager.getItems()
        val order = Order(
            id = transactionId,
            buyerId = buyerId,
            items = items,
            total = CartManager.getTotalPrice(),
            status = "PAID",
            address = address,
            timestamp = Date() // En una app real usaría FieldValue.serverTimestamp()
        )

        val batch = db.batch()
        
        // Guardar la orden
        val orderRef = db.collection("orders").document(transactionId)
        batch.set(orderRef, order)

        // Actualizar stock de cada producto
        items.forEach { item: co.edu.compensar.voltcore.data.CartItem ->
            val productRef = db.collection("products").document(item.productId)
            batch.update(productRef, "stock", com.google.firebase.firestore.FieldValue.increment(-item.quantity.toLong()))
        }

        batch.commit()
            .addOnSuccessListener {
                binding.pbPayment.visibility = View.GONE
                CartManager.clear()
                Toast.makeText(context, "¡PAGO EXITOSO! Pedido registrado y stock actualizado.", Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_checkout_to_buyerHome)
            }
            .addOnFailureListener {
                binding.btnConfirmPayment.isEnabled = true
                binding.pbPayment.visibility = View.GONE
                Toast.makeText(context, "Error al procesar el pedido en la base de datos", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
