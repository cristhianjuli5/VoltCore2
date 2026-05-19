package co.edu.compensar.voltcore.ui.vendor

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import co.edu.compensar.voltcore.data.Product
import co.edu.compensar.voltcore.databinding.FragmentVendorProductFormBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class VendorProductFormFragment : Fragment() {
    private var _binding: FragmentVendorProductFormBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private var capturedImageUri: Uri? = null
    private var productId: String? = null
    private var currentImageUrl: String? = null

    // Client ID de Imgur (Uso uno de prueba, en producción deberías crear uno propio)
    private val IMGUR_CLIENT_ID = "3954546452292f7" 

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val cameraPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) startCamera()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVendorProductFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productId = arguments?.getString("productId")
        if (productId != null) loadProductData(productId!!)

        setupCategorySelector()

        if (allPermissionsGranted()) startCamera() else cameraPermissionRequest.launch(Manifest.permission.CAMERA)

        binding.btnCapture.setOnClickListener { takePhoto() }
        binding.btnSaveProduct.setOnClickListener { saveProduct() }
        
        binding.ivProductPreview.setOnClickListener {
            binding.ivProductPreview.visibility = View.GONE
            binding.viewFinder.visibility = View.VISIBLE
            capturedImageUri = null
        }
    }

    private fun setupCategorySelector() {
        db.collection("products")
            .get()
            .addOnSuccessListener { snapshot ->
                val categories = snapshot.documents
                    .mapNotNull { it.getString("category") }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .sorted()
                
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
                (binding.etProductCategory as? AutoCompleteTextView)?.setAdapter(adapter)
            }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also { it.setSurfaceProvider(binding.viewFinder.surfaceProvider) }
            imageCapture = ImageCapture.Builder().build()
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture)
            } catch (exc: Exception) { Log.e("Imgur", "Error cámara", exc) }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(requireContext().externalCacheDir, "temp_img.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) { Toast.makeText(context, "Error capturando", Toast.LENGTH_SHORT).show() }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    capturedImageUri = Uri.fromFile(photoFile)
                    binding.ivProductPreview.setImageURI(capturedImageUri)
                    binding.ivProductPreview.visibility = View.VISIBLE
                    binding.viewFinder.visibility = View.GONE
                }
            })
    }

    private fun saveProduct() {
        val name = binding.etProductName.text.toString().trim()
        val emoji = binding.etProductEmoji.text.toString().trim()
        
        if (name.isEmpty()) {
            Toast.makeText(context, "El nombre es obligatorio", Toast.LENGTH_SHORT).show()
            return
        }

        if (emoji.isEmpty() && capturedImageUri == null && currentImageUrl == null) {
            Toast.makeText(context, "Debes poner un Emoji o tomar una Foto", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveProduct.isEnabled = false

        if (emoji.isNotEmpty()) {
            saveToFirestore(emoji)
        } else if (capturedImageUri != null) {
            uploadToImgur(capturedImageUri!!) { url ->
                if (url != null) saveToFirestore(url)
                else {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveProduct.isEnabled = true
                    Toast.makeText(context, "Error al subir a Imgur. Prueba usando un Emoji.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            saveToFirestore(currentImageUrl!!)
        }
    }

    private fun uploadToImgur(uri: Uri, callback: (String?) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val bytes = requireContext().contentResolver.openInputStream(uri)?.readBytes() ?: return@launch
                val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)

                val url = URL("https://api.imgur.com/3/image")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Authorization", "Client-ID $IMGUR_CLIENT_ID")
                conn.doOutput = true

                val body = "image=" + java.net.URLEncoder.encode(base64Image, "UTF-8")
                conn.outputStream.use { it.write(body.toByteArray()) }

                if (conn.responseCode == 200) {
                    val response = conn.inputStream.bufferedReader().use { it.readText() }
                    val link = JSONObject(response).getJSONObject("data").getString("link")
                    withContext(Dispatchers.Main) { callback(link) }
                } else {
                    withContext(Dispatchers.Main) { callback(null) }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { callback(null) }
            }
        }
    }

    private fun saveToFirestore(imageUrl: String) {
        val product = Product(
            id = productId ?: "",
            name = binding.etProductName.text.toString(),
            description = binding.etProductDesc.text.toString(),
            price = binding.etProductPrice.text.toString().toDoubleOrNull() ?: 0.0,
            stock = binding.etProductStock.text.toString().toIntOrNull() ?: 0,
            imageUrl = imageUrl,
            vendorId = auth.currentUser?.uid ?: "",
            category = binding.etProductCategory.text.toString()
        )

        val ref = if (productId == null) db.collection("products").document() else db.collection("products").document(productId!!)
        ref.set(product.copy(id = ref.id)).addOnSuccessListener {
            findNavController().popBackStack()
        }
    }

    private fun loadProductData(id: String) {
        db.collection("products").document(id).get().addOnSuccessListener { doc ->
            val p = doc.toObject(Product::class.java)
            p?.let {
                binding.etProductName.setText(it.name)
                binding.etProductDesc.setText(it.description)
                binding.etProductPrice.setText(it.price.toString())
                binding.etProductStock.setText(it.stock.toString())
                binding.etProductCategory.setText(it.category)
                currentImageUrl = it.imageUrl
                
                if (it.imageUrl.length <= 4) {
                    binding.etProductEmoji.setText(it.imageUrl)
                } else {
                    binding.ivProductPreview.visibility = View.VISIBLE
                    binding.viewFinder.visibility = View.GONE
                    Glide.with(this).load(it.imageUrl).into(binding.ivProductPreview)
                }
            }
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
