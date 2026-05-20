package co.edu.compensar.voltcore.ui.vendor

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
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
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.data.Product
import co.edu.compensar.voltcore.databinding.FragmentVendorProductFormBinding
import co.edu.compensar.voltcore.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class VendorProductFormFragment : Fragment() {
    private var _binding: FragmentVendorProductFormBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private var capturedImageUri: Uri? = null
    private var productId: String? = null
    private var currentImageUrl: String? = null

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val cameraPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) startCamera()
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            capturedImageUri = it
            binding.ivProductPreview.setImageURI(it)
            binding.ivProductPreview.visibility = View.VISIBLE
            binding.viewFinder.visibility = View.GONE
        }
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
        binding.btnGallery.setOnClickListener { galleryLauncher.launch("image/*") }
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
                val currentContext = context ?: return@addOnSuccessListener
                if (_binding == null) return@addOnSuccessListener
                
                val categories = snapshot.documents
                    .mapNotNull { it.getString("category") }
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .sorted()
                
                val adapter = ArrayAdapter(currentContext, android.R.layout.simple_dropdown_item_1line, categories)
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
            } catch (exc: Exception) { Log.e("Camera", "Error cámara", exc) }
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
            processAndSaveImage(capturedImageUri!!)
        } else {
            saveToFirestore(currentImageUrl!!)
        }
    }

    private fun processAndSaveImage(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentContext = context ?: return@launch
                val inputStream = currentContext.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                
                // Redimensionar a 400px y calidad 50% para que pese MUY POCO
                val base64Image = ImageUtils.compressBitmapToBase64(originalBitmap, quality = 50, maxSize = 400)
                
                withContext(Dispatchers.Main) {
                    saveToFirestore("data:image/jpeg;base64,$base64Image")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (_binding != null) {
                        binding.progressBar.visibility = View.GONE
                        binding.btnSaveProduct.isEnabled = true
                        Toast.makeText(context, "Error procesando imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
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
        ref.set(product.copy(id = ref.id))
            .addOnSuccessListener {
                if (_binding != null) {
                    Toast.makeText(context, "Producto guardado con éxito", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
            .addOnFailureListener { e ->
                if (_binding != null) {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveProduct.isEnabled = true
                    // Mostrar error específico (podría ser por tamaño o reglas)
                    Log.e("FirestoreError", "Error al guardar", e)
                    Toast.makeText(context, "Error al guardar: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun loadProductData(id: String) {
        db.collection("products").document(id).get().addOnSuccessListener { doc ->
            if (_binding == null) return@addOnSuccessListener
            val p = doc.toObject(Product::class.java)
            p?.let {
                binding.etProductName.setText(it.name)
                binding.etProductDesc.setText(it.description)
                binding.etProductPrice.setText(it.price.toString())
                binding.etProductStock.setText(it.stock.toString())
                binding.etProductCategory.setText(it.category)
                currentImageUrl = it.imageUrl
                
                ImageUtils.loadImage(
                    requireContext(),
                    it.imageUrl,
                    binding.ivProductPreview,
                    emojiTextView = null // En el form no usamos el textview para emoji
                )
                
                if (it.imageUrl.length > 4) {
                    binding.ivProductPreview.visibility = View.VISIBLE
                    binding.viewFinder.visibility = View.GONE
                }
            }
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
