package co.edu.compensar.voltcore.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import co.edu.compensar.voltcore.R
import co.edu.compensar.voltcore.databinding.DialogEditUserBinding
import co.edu.compensar.voltcore.databinding.FragmentAdminUsersBinding
import co.edu.compensar.voltcore.databinding.ItemUserAdminBinding

class AdminUsersFragment : Fragment() {
    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!
    
    private val userList = mutableListOf(
        User(1, "Carlos Admin", "admin@voltcore.com", "Administrador"),
        User(2, "Juan Vendedor", "juan@test.com", "Vendedor"),
        User(3, "Maria Compradora", "maria@gmail.com", "Comprador")
    )

    private lateinit var adapter: UserAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = UserAdapter(userList, 
            onEdit = { user -> showUserDialog(user) },
            onDelete = { user -> deleteUser(user) }
        )
        binding.rvUsers.adapter = adapter

        binding.fabAddUser.setOnClickListener {
            showUserDialog()
        }
    }

    private fun showUserDialog(user: User? = null) {
        val dialogBinding = DialogEditUserBinding.inflate(LayoutInflater.from(requireContext()))
        
        val roles = arrayOf("Administrador", "Vendedor", "Comprador")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spUserRole.adapter = spinnerAdapter

        user?.let {
            dialogBinding.etUserName.setText(it.name)
            dialogBinding.etUserEmail.setText(it.email)
            val selection = roles.indexOf(it.role)
            if (selection >= 0) dialogBinding.spUserRole.setSelection(selection)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (user == null) "Crear Usuario" else "Editar Usuario")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar") { _, _ ->
                val name = dialogBinding.etUserName.text.toString()
                val email = dialogBinding.etUserEmail.text.toString()
                val role = dialogBinding.spUserRole.selectedItem.toString()

                if (name.isNotEmpty() && email.isNotEmpty()) {
                    if (user == null) {
                        val newId = (userList.maxOfOrNull { it.id } ?: 0) + 1
                        val newUser = User(newId, name, email, role)
                        userList.add(0, newUser)
                        adapter.notifyItemInserted(0)
                        binding.rvUsers.scrollToPosition(0)
                        Toast.makeText(requireContext(), "Usuario creado", Toast.LENGTH_SHORT).show()
                    } else {
                        val index = userList.indexOfFirst { it.id == user.id }
                        if (index != -1) {
                            userList[index] = user.copy(name = name, email = email, role = role)
                            adapter.notifyItemChanged(index)
                            Toast.makeText(requireContext(), "Usuario actualizado", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteUser(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Usuario")
            .setMessage("¿Estás seguro de que deseas eliminar a ${user.name}?")
            .setPositiveButton("Eliminar") { _, _ ->
                val index = userList.indexOfFirst { it.id == user.id }
                if (index != -1) {
                    userList.removeAt(index)
                    adapter.notifyItemRemoved(index)
                    Toast.makeText(requireContext(), "Usuario eliminado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    class UserAdapter(
        private val users: List<User>,
        private val onEdit: (User) -> Unit,
        private val onDelete: (User) -> Unit
    ) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

        class UserViewHolder(val binding: ItemUserAdminBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val binding = ItemUserAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return UserViewHolder(binding)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            val user = users[position]
            with(holder.binding) {
                tvUserName.text = user.name
                tvUserEmail.text = user.email
                tvUserRole.text = user.role
                
                btnEditUser.setOnClickListener { onEdit(user) }
                btnDeleteUser.setOnClickListener { onDelete(user) }
            }
        }

        override fun getItemCount() = users.size
    }

    data class User(val id: Int, val name: String, val email: String, val role: String)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
