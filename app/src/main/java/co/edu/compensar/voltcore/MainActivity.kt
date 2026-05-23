package co.edu.compensar.voltcore

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import co.edu.compensar.voltcore.databinding.ActivityMainBinding

/**
 * Actividad principal que actúa como contenedor para la navegación de la aplicación.
 * Gestiona la configuración del Toolbar, DrawerLayout y BottomNavigationView,
 * así como la lógica de cierre de sesión y actualización de la interfaz según el destino.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflar el layout usando View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar el Toolbar como ActionBar de la actividad
        setSupportActionBar(binding.toolbar)

        // Obtener el NavController desde el NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Definir los destinos de nivel superior donde no se debe mostrar el botón de retroceso
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.loginFragment,
                R.id.buyerHomeFragment,
                R.id.vendorDashboardFragment,
                R.id.adminDashboardFragment
            ),
            binding.drawerLayout
        )

        // Vincular el NavController con los componentes de la interfaz
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
        binding.bottomNavigation.setupWithNavController(navController)

        // Manejar eventos de clic en el Navigation Drawer
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.logout_action) {
                logout()
                true
            } else {
                // Delegar la navegación al Navigation Component
                val handled = androidx.navigation.ui.NavigationUI.onNavDestinationSelected(menuItem, navController)
                if (handled) binding.drawerLayout.closeDrawers()
                handled
            }
        }

        // Manejar eventos de clic en el Bottom Navigation
        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.logout_action) {
                logout()
                true
            } else {
                androidx.navigation.ui.NavigationUI.onNavDestinationSelected(menuItem, navController)
            }
        }

        // Escuchar cambios de destino para ajustar la visibilidad de elementos comunes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateNavigationUI(destination.id)
        }
    }

    /**
     * Actualiza la visibilidad del Toolbar y BottomNavigationView, y el menú del Drawer
     * según el rol del usuario y el destino actual.
     */
    private fun updateNavigationUI(destinationId: Int) {
        when (destinationId) {
            // Ocultar barras en pantallas de inicio de sesión o splash
            R.id.splashFragment, R.id.loginFragment, R.id.recoveryFragment -> {
                binding.toolbar.visibility = View.GONE
                binding.bottomNavigation.visibility = View.GONE
                binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            // Configuración para el flujo de Comprador
            R.id.buyerHomeFragment, R.id.catalogFragment, R.id.cartFragment, R.id.productDetailFragment, R.id.checkoutFragment, R.id.buyerProfileFragment, R.id.buyerOrdersFragment -> {
                binding.toolbar.visibility = View.VISIBLE
                binding.bottomNavigation.visibility = View.VISIBLE
                binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED)
                binding.navView.menu.clear()
                binding.navView.inflateMenu(R.menu.buyer_drawer_menu)
            }
            // Configuración para el flujo de Vendedor
            R.id.vendorDashboardFragment, R.id.vendorProductsFragment, R.id.vendorOrdersFragment, R.id.vendorProfileFragment, R.id.vendorProductFormFragment -> {
                binding.toolbar.visibility = View.VISIBLE
                binding.bottomNavigation.visibility = View.GONE
                binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED)
                binding.navView.menu.clear()
                binding.navView.inflateMenu(R.menu.vendor_drawer_menu)
            }
            // Configuración para el flujo de Administrador
            R.id.adminDashboardFragment, R.id.adminUsersFragment, R.id.adminModerationFragment, R.id.adminReportsFragment, R.id.adminProductsFragment -> {
                binding.toolbar.visibility = View.VISIBLE
                binding.bottomNavigation.visibility = View.GONE
                binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED)
                binding.navView.menu.clear()
                binding.navView.inflateMenu(R.menu.admin_drawer_menu)
            }
        }
    }

    /**
     * Cierra la sesión del usuario en Firebase y redirige a la pantalla de login.
     */
    private fun logout() {
        // Cerrar sesión en Firebase
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

        // Navegar a login y limpiar todo el historial de navegación
        navController.navigate(R.id.loginFragment, null, androidx.navigation.NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build())
    }

    /**
     * Gestiona el comportamiento del botón "Atrás" en la barra de acciones.
     */
    override fun onSupportNavigateUp(): Boolean {
        return androidx.navigation.ui.NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}
