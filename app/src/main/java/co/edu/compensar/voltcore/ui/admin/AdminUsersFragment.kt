package co.edu.compensar.voltcore.ui.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import co.edu.compensar.voltcore.data.User
import co.edu.compensar.voltcore.data.UserRole
import co.edu.compensar.voltcore.databinding.DialogEditUserBinding
import co.edu.compensar.voltcore.databinding.FragmentAdminUsersBinding
import co.edu.compensar.voltcore.databinding.ItemUserAdminBinding
import com.google.firebase.firestore.FirebaseFirestore

class AdminUsersFragment : Fragment() {
    private var _binding: FragmentAdminUsersBinding? = null
    private val binding get() = _binding!!
    
    private val db by lazy { FirebaseFirestore.getInstance() }
    private val userList = mutableListOf<User>()
    private var filteredList = mutableListOf<User>()
    private lateinit var adapter: UserAdapter
    private var snapshotListener: com.google.firebase.firestore.ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdminUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = UserAdapter(filteredList, 
            onEdit = { user -> showUserDialog(user) },
            onDelete = { user -> deleteUser(user) }
        )
        binding.rvUsers.adapter = adapter

        binding.fabAddUser.setOnClickListener {
            showUserDialog()
        }

        setupSearch()
        fetchUsers()
    }

    private fun setupSearch() {
        binding.etSearchUsers.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterUsers(query: String) {
        val lowercaseQuery = query.lowercase()
        filteredList.clear()
        if (lowercaseQuery.isEmpty()) {
            filteredList.addAll(userList)
        } else {
            for (user in userList) {
                if (user.name.lowercase().contains(lowercaseQuery) || 
                    user.email.lowercase().contains(lowercaseQuery)) {
                    filteredList.add(user)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun fetchUsers() {
        snapshotListener = db.collection("users").addSnapshotListener { snapshot, e ->
            if (_binding == null) return@addSnapshotListener
            if (e != null) {
                context?.let {
                    Toast.makeText(it, "Error al cargar usuarios", Toast.LENGTH_SHORT).show()
                }
                return@addSnapshotListener
            }
            if (snapshot != null) {
                userList.clear()
                for (doc in snapshot.documents) {
                    val user = doc.toObject(User::class.java)
                    user?.let {
                        userList.add(it.copy(uid = doc.id))
                    }
                }
                filterUsers(binding.etSearchUsers.text.toString())
            }
        }
    }

    private fun showUserDialog(user: User? = null) {
        val dialogBinding = DialogEditUserBinding.inflate(LayoutInflater.from(requireContext()))
        
        val roles = UserRole.values().map { it.name }.toTypedArray()
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, roles)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        dialogBinding.spUserRole.adapter = spinnerAdapter

        user?.let {
            dialogBinding.etUserName.setText(it.name)
            dialogBinding.etUserEmail.setText(it.email)
            dialogBinding.etUserAddress.setText(it.address)
            val selection = roles.indexOf(it.role.name)
            if (selection >= 0) dialogBinding.spUserRole.setSelection(selection)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(if (user == null) "Crear Usuario" else "Editar Usuario")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar", null) // Set to null to prevent auto-dismiss
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.etUserName.text.toString().trim()
                val email = dialogBinding.etUserEmail.text.toString().trim()
                val address = dialogBinding.etUserAddress.text.toString().trim()
                val roleStr = dialogBinding.spUserRole.selectedItem.toString()
                val role = UserRole.valueOf(roleStr)

                if (validateFields(dialogBinding, name, email)) {
                    val userId = user?.uid ?: db.collection("users").document().id
                    val updatedUser = User(
                        uid = userId,
                        name = name,
                        email = email,
                        role = role,
                        address = address
                    )
                    
                    db.collection("users").document(userId).set(updatedUser)
                        .addOnSuccessListener { 
                            Toast.makeText(context, if (user == null) "Usuario creado" else "Usuario actualizado", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Error al guardar: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
        dialog.show()
    }

    private fun validateFields(binding: DialogEditUserBinding, name: String, email: String): Boolean {
        var isValid = true
        
        if (name.isEmpty()) {
            binding.etUserName.error = "El nombre es obligatorio"
            isValid = false
        }
        
        if (email.isEmpty()) {
            binding.etUserEmail.error = "El correo es obligatorio"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etUserEmail.error = "Correo no válido"
            isValid = false
        }
        
        return isValid
    }

    private fun deleteUser(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar Usuario")
            .setMessage("¿Estás seguro de que deseas eliminar a ${user.name}?")
            .setPositiveButton("Eliminar") { _, _ ->
                db.collection("users").document(user.uid).delete()
                    .addOnSuccessListener { Toast.makeText(context, "Usuario eliminado", Toast.LENGTH_SHORT).show() }
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
                tvUserRole.text = user.role.name
                
                btnEditUser.setOnClickListener { onEdit(user) }
                btnDeleteUser.setOnClickListener { onDelete(user) }
            }
        }

        override fun getItemCount() = users.size
    }

    override fun onDestroyView() {
        super.onDestroyView()
        snapshotListener?.remove()
        _binding = null
    }
}
