package co.edu.compensar.voltcore.ui.vendor

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.edu.compensar.voltcore.data.Product
import co.edu.compensar.voltcore.databinding.FragmentVendorProductFormBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class VendorProductFormFragment : Fragment() {
    private var _binding: FragmentVendorProductFormBinding? = null
    private val binding get() = _binding!!

    private var imageCapture: ImageCapture? = null
    private var capturedImageUri: Uri? = null
    private var productId: String? = null
    private var currentImageUrl: String? = null

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            capturedImageUri = it
            binding.ivProductPreview.setImageURI(it)
            binding.ivProductPreview.visibility = View.VISIBLE
            binding.viewFinder.visibility = View.GONE
        }
    }

    private val db by lazy { FirebaseFirestore.getInstance() }
    private val storage by lazy { FirebaseStorage.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val cameraPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(context, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVendorProductFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        productId = arguments?.getString("productId")
        if (productId != null) {
            loadProductData(productId!!)
            binding.btnSaveProduct.text = "ACTUALIZAR PRODUCTO"
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            cameraPermissionRequest.launch(Manifest.permission.CAMERA)
        }

        binding.btnCapture.setOnClickListener {
            takePhoto()
        }

        binding.btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.btnSaveProduct.setOnClickListener {
            saveProduct()
        }
    }

    private fun loadProductData(id: String) {
        db.collection("products").document(id).get()
            .addOnSuccessListener { doc ->
                val product = doc.toObject(Product::class.java)
                product?.let {
                    binding.etProductName.setText(it.name)
                    binding.etProductDesc.setText(it.description)
                    binding.etProductPrice.setText(it.price.toString())
                    binding.etProductStock.setText(it.stock.toString())
                    binding.etProductCategory.setText(it.category)
                    currentImageUrl = it.imageUrl
                    
                    if (it.imageUrl.isNotEmpty()) {
                        binding.ivProductPreview.visibility = View.VISIBLE
                        binding.viewFinder.visibility = View.GONE
                        Glide.with(this).load(it.imageUrl).into(binding.ivProductPreview)
                    }
                }
            }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e("VendorProductForm", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            requireContext().externalCacheDir,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e("VendorProductForm", "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    capturedImageUri = Uri.fromFile(photoFile)
                    binding.ivProductPreview.setImageURI(capturedImageUri)
                    binding.ivProductPreview.visibility = View.VISIBLE
                    binding.viewFinder.visibility = View.GONE
                    Toast.makeText(context, "Foto capturada", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun saveProduct() {
        val name = binding.etProductName.text.toString()
        val desc = binding.etProductDesc.text.toString()
        val price = binding.etProductPrice.text.toString().toDoubleOrNull() ?: 0.0
        val stock = binding.etProductStock.text.toString().toIntOrNull() ?: 0
        val category = binding.etProductCategory.text.toString()

        if (name.isEmpty() || (capturedImageUri == null && currentImageUrl == null)) {
            Toast.makeText(context, "Completa nombre e imagen", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveProduct.isEnabled = false

        if (capturedImageUri != null) {
            uploadImageAndSave(name, desc, price, stock, category)
        } else {
            saveToFirestore(name, desc, price, stock, category, currentImageUrl ?: "")
        }
    }

    private fun uploadImageAndSave(name: String, desc: String, price: Double, stock: Int, category: String) {
        val fileName = UUID.randomUUID().toString()
        val ref = storage.reference.child("products/$fileName.jpg")

        ref.putFile(capturedImageUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    saveToFirestore(name, desc, price, stock, category, uri.toString())
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.btnSaveProduct.isEnabled = true
                Toast.makeText(context, "Error al subir imagen", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveToFirestore(name: String, desc: String, price: Double, stock: Int, category: String, imageUrl: String) {
        val vendorId = auth.currentUser?.uid ?: return
        val product = Product(
            id = productId ?: "",
            name = name,
            description = desc,
            price = price,
            stock = stock,
            imageUrl = imageUrl,
            vendorId = vendorId,
            category = category
        )

        val task = if (productId == null) {
            db.collection("products").add(product)
        } else {
            db.collection("products").document(productId!!).set(product)
        }

        task.addOnSuccessListener {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(context, "Producto guardado", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
        }.addOnFailureListener {
            binding.progressBar.visibility = View.GONE
            binding.btnSaveProduct.isEnabled = true
            Toast.makeText(context, "Error al guardar producto", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
