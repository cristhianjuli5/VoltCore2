package co.edu.compensar.voltcore.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.data.CartItem
import co.edu.compensar.voltcore.data.CartManager
import co.edu.compensar.voltcore.databinding.FragmentCartBinding

import co.edu.compensar.voltcore.databinding.ItemCartProductBinding

/**
 * Fragmento que muestra los productos en el carrito de compras.
 * Permite al usuario modificar cantidades, eliminar productos y proceder al pago.
 */
class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Inicializar la lista de productos del carrito
        setupCartList()
        
        // Configurar el botón de pago (checkout)
        binding.btnGoToCheckout.setOnClickListener {
            if (CartManager.getCount() > 0) {
                // Navegar a la pantalla de pago si hay productos
                findNavController().navigate(R.id.action_cart_to_checkout)
            } else {
                // Notificar si el carrito está vacío usando recursos de strings
                android.widget.Toast.makeText(context, getString(R.string.empty_cart_error), android.widget.Toast.LENGTH_SHORT).show()
            }
        }
        
        // Inicializar el total de la compra
        updateTotal()
    }

    /**
     * Configura el RecyclerView con los elementos actuales del carrito.
     */
    private fun setupCartList() {
        binding.rvCartItems.layoutManager = LinearLayoutManager(context)
        binding.rvCartItems.adapter = object : RecyclerView.Adapter<CartViewHolder>() {
            private val items = CartManager.getItems()

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
                val binding = ItemCartProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return CartViewHolder(binding)
            }

            override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
                val item = items[position]
                holder.binding.tvCartProductName.text = item.productName
                holder.binding.tvCartProductPrice.text = "$ %,.0f".format(item.price)
                holder.binding.tvQuantity.text = item.quantity.toString()

                // Incrementar cantidad
                holder.binding.btnPlus.setOnClickListener {
                    val currentPos = holder.adapterPosition
                    if (currentPos != RecyclerView.NO_POSITION) {
                        val currentItem = items[currentPos]
                        CartManager.updateQuantity(currentItem.productId, currentItem.quantity + 1)
                        notifyItemChanged(currentPos)
                        updateTotal()
                    }
                }

                // Decrementar cantidad o eliminar si es 1
                holder.binding.btnMinus.setOnClickListener {
                    val currentPos = holder.adapterPosition
                    if (currentPos != RecyclerView.NO_POSITION) {
                        val currentItem = items[currentPos]
                        if (currentItem.quantity > 1) {
                            CartManager.updateQuantity(currentItem.productId, currentItem.quantity - 1)
                            notifyItemChanged(currentPos)
                        } else {
                            CartManager.removeItem(currentItem.productId)
                            notifyDataSetChanged()
                        }
                        updateTotal()
                    }
                }
            }

            override fun getItemCount() = items.size
        }
    }

    /**
     * Calcula y muestra el precio total actualizado del carrito.
     */
    private fun updateTotal() {
        val total = CartManager.getTotalPrice()
        binding.tvTotal.text = "$ %,.0f".format(total)
    }

    /**
     * ViewHolder para representar cada producto en la lista del carrito.
     */
    class CartViewHolder(val binding: ItemCartProductBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onDestroyView() {
        super.onDestroyView()
        // Evitar fugas de memoria limpiando el binding
        _binding = null
    }
}
