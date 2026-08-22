package com.example.progetto_rosso_iacopo.ui.auth

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.progetto_rosso_iacopo.R
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.progetto_rosso_iacopo.MainActivity
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.firebase.ui.auth.AuthUI


class WelcomeActivity : AppCompatActivity() {
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build(),
    )

    fun signOut(){
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {

            }
    }

    fun signIn(){
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
    }

    fun startMainActivity(){
        val MainActivityIntent = Intent(this, MainActivity::class.java)
        startActivity(MainActivityIntent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (FirebaseAuth.getInstance().currentUser != null) {
            startMainActivity()
            return
        }
        setContentView(R.layout.activity_welcome)

        val signInButton = findViewById<Button>(R.id.signInButton)
        signInButton.setOnClickListener { signIn() }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        } else {
            if (response == null) {
                android.util.Log.d(
                    "AUTH_DEBUG",
                    "L'utente ha annullato il flusso usando il tasto Back."
                )
            } else {
                val errorCode: Int? = response.error?.errorCode
                val errorMessage: String = response.error?.localizedMessage ?: "Errore di autenticazione"
                android.util.Log.e(
                    "AUTH_DEBUG",
                    "Errore FirebaseUI. Codice: $errorCode, Messaggio: $errorMessage"
                )
                android.widget.Toast.makeText(
                    this,
                    "Accesso fallito: $errorMessage (Codice: $errorCode)",
                    android.widget.Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}