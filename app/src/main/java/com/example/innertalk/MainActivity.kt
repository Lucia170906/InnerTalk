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
                R.id.statisticsFragment
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
}

