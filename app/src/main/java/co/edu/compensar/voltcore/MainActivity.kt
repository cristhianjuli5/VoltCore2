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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Define top-level destinations (no back button)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.loginFragment,
                R.id.buyerHomeFragment,
                R.id.vendorDashboardFragment,
                R.id.adminDashboardFragment
            ),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
        binding.bottomNavigation.setupWithNavController(navController)

        // Handle logout for both Drawer and Bottom Navigation
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.logout_action) {
                logout()
                true
            } else {
                val handled = androidx.navigation.ui.NavigationUI.onNavDestinationSelected(menuItem, navController)
                if (handled) binding.drawerLayout.closeDrawers()
                handled
            }
        }

        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.logout_action) {
                logout()
                true
            } else {
                androidx.navigation.ui.NavigationUI.onNavDestinationSelected(menuItem, navController)
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateNavigationUI(destination.id)
        }
    }

    private fun updateNavigationUI(destinationId: Int) {
        when (destinationId) {
            R.id.splashFragment, R.id.loginFragment, R.id.recoveryFragment -> {
                binding.toolbar.visibility = View.GONE
                binding.bottomNavigation.visibility = View.GONE
                binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            R.id.buyerHomeFragment, R.id.catalogFragment, R.id.cartFragment, R.id.productDetailFragment, R.id.checkoutFragment, R.id.buyerProfileFragment, R.id.buyerOrdersFragment -> {
                binding.toolbar.visibility = View.VISIBLE
                binding.bottomNavigation.visibility = View.VISIBLE
                binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
            R.id.vendorDashboardFragment, R.id.vendorProductsFragment, R.id.vendorOrdersFragment, R.id.vendorProfileFragment, R.id.vendorProductFormFragment -> {
                binding.toolbar.visibility = View.VISIBLE
                binding.bottomNavigation.visibility = View.GONE
                binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED)
                binding.navView.menu.clear()
                binding.navView.inflateMenu(R.menu.vendor_drawer_menu)
            }
            R.id.adminDashboardFragment, R.id.adminUsersFragment, R.id.adminModerationFragment, R.id.adminReportsFragment, R.id.adminProductsFragment -> {
                binding.toolbar.visibility = View.VISIBLE
                binding.bottomNavigation.visibility = View.GONE
                binding.drawerLayout.setDrawerLockMode(androidx.drawerlayout.widget.DrawerLayout.LOCK_MODE_UNLOCKED)
                binding.navView.menu.clear()
                binding.navView.inflateMenu(R.menu.admin_drawer_menu)
            }
        }
    }

    private fun logout() {
        // Cerrar sesión en Firebase
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

        // Navegar a login y limpiar todo el historial
        navController.navigate(R.id.loginFragment, null, androidx.navigation.NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build())
    }

    override fun onSupportNavigateUp(): Boolean {
        return androidx.navigation.ui.NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}
