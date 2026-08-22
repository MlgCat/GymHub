package com.example.progetto_rosso_iacopo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.progetto_rosso_iacopo.ui.theme.Progetto_Rosso_IacopoTheme
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.MaterialToolbar
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.ui.AppBarConfiguration
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Trova il NavHostFragment e il relativo NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // 2. Trova la Toolbar e configurala con il Navigation Component
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.dashboardFragment) // Schermate principali (senza freccia indietro)
        )

        toolbar.setupWithNavController(navController, appBarConfiguration)
    }
}