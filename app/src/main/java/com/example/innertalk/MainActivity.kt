package com.example.innertalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.navigateUp
import com.example.innertalk.databinding.MainLayoutBinding
import com.example.innertalk.ui.theme.InnerTalkTheme

class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainLayoutBinding

    private lateinit var navController : NavController

    private lateinit var appBarConfiguration : AppBarConfiguration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //configuramos toolbar
        setSupportActionBar(binding.toolbar)

        //Obtenemos el NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        //Configuramos la AppBar con los destinos del bottomMenu

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.chatFragment2,
                R.id.newEntryFragment,
                R.id.statisticsFragment,
                R.id.startFragment
            )
        )

        //Vinculamos Toolbar con NavController
        NavigationUI.setupActionBarWithNavController(
            this,
            navController,
            appBarConfiguration
        )

        //Sincronizamos BottomNavigationView con el NavController
        NavigationUI.setupWithNavController(
            binding.bottomNavigationView,
            navController
        )

    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
//    // ... después de onSupportNavigateUp ...
//
//    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
//        // Infla el menú de la toolbar (asegúrate de que el nombre del archivo sea correcto)
//        menuInflater.inflate(R.menu.menu_options, menu)
//        return true
//    }
//
//    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
//        // Maneja el clic en el botón de ajustes
//        return when (item.itemId) {
//            R.id.configFragment -> { // El ID debe coincidir con el de tu nav_graph
//                navController.navigate(R.id.configFragment)
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }
}

