package co.edu.compensar.voltcore.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import co.edu.compensar.voltcore.R
import com.bumptech.glide.Glide
import java.io.ByteArrayOutputStream

object ImageUtils {

    /**
     * Carga una imagen de forma inteligente. Soporta:
     * 1. Emojis (strings cortos)
     * 2. URLs de Internet (http/https)
     * 3. Datos Base64 (data:image/...)
     */
    fun loadImage(
        context: Context,
        imageUrl: String,
        imageView: ImageView,
        emojiTextView: TextView? = null,
        placeholder: Int = R.drawable.ic_voltcore_logo
    ) {
        if (imageUrl.isEmpty()) {
            imageView.setImageResource(placeholder)
            emojiTextView?.visibility = View.GONE
            imageView.visibility = View.VISIBLE
            return
        }

        // Caso 1: Es un Emoji
        if (imageUrl.length <= 4) {
            imageView.visibility = View.GONE
            emojiTextView?.let {
                it.visibility = View.VISIBLE
                it.text = imageUrl
            }
            return
        }

        // Caso 2: Es Base64 o URL
        imageView.visibility = View.VISIBLE
        emojiTextView?.visibility = View.GONE

        if (imageUrl.startsWith("data:image")) {
            try {
                val pureBase64 = imageUrl.substringAfter(",")
                val imageBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                Glide.with(context)
                    .asBitmap()
                    .load(imageBytes)
                    .placeholder(placeholder)
                    .into(imageView)
            } catch (e: Exception) {
                imageView.setImageResource(placeholder)
            }
        } else {
            // Es una URL normal (o ruta local si se usó ese método)
            Glide.with(context)
                .load(imageUrl)
                .placeholder(placeholder)
                .into(imageView)
        }
    }

    /**
     * Convierte un Bitmap a una cadena Base64 comprimida
     */
    fun compressBitmapToBase64(bitmap: Bitmap, quality: Int = 50, maxSize: Int = 400): String {
        // 1. Redimensionar
        var width = bitmap.width
        var height = bitmap.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        
        val resized = Bitmap.createScaledBitmap(bitmap, width, height, true)

        // 2. Comprimir
        val outputStream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }
}
