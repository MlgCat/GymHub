package com.example.progetto_rosso_iacopo

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
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
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.MaterialToolbar
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.example.progetto_rosso_iacopo.ui.auth.WelcomeActivity
import com.firebase.ui.auth.AuthUI
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.dashboardFragment) // Schermate principali (senza freccia indietro)
        )
        toolbar.setupWithNavController(navController, appBarConfiguration)
        toolbar.setNavigationOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.routineDetailFragment -> {
                    navController.popBackStack(R.id.dashboardFragment, false)
                }
                R.id.addWorkoutFragment-> {
                    navController.popBackStack(R.id.dashboardFragment, false)
                }
                else -> {
                    // comportamento standard per tutti gli altri
                    if (!navController.navigateUp(appBarConfiguration)) {
                        finish()
                    }
                }
            }
        }
        toolbar.inflateMenu(R.menu.main_menu)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener {
                            val intent = Intent(this, WelcomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    true
                }
                else -> false
            }
        }

// Aggiunge il menu alla toolbar
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_logout -> {
                        AuthUI.getInstance()
                            .signOut(this@MainActivity)
                            .addOnCompleteListener {
                                val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        true
                    }
                    else -> false
                }
            }
        }, this, Lifecycle.State.RESUMED)

        fun logout() {
            AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    val WelcomeActivityIntent = Intent(this, WelcomeActivity::class.java)
                    startActivity(WelcomeActivityIntent)
                    finish()
                }
        }

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (!granted) {
                Toast.makeText(this ,"l'applicazione non ha i permessi per le notifiche", Toast.LENGTH_SHORT).show()
            }
        }
        //permessi per impostare svaeglie in background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // manda l'utente al permesso schedule exact alarm
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            }
        }
    }
}
