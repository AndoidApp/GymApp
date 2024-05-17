package com.example.gymapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WelcomeActivity : AppCompatActivity() {
    /*
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    private fun checkIfEmailExists(email: String, onSuccess: () -> Unit, onFailure: () -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        usersCollection.whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    onSuccess()
                } else {
                    onFailure()
                }
            }
            .addOnFailureListener { e ->
                onFailure()
                Log.e("Read users DB", "ERROR | users query", e)
            }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult?) {
        val db = FirebaseFirestore.getInstance()
        val usersCollection = db.collection("users")

        if (result != null) {
            val response = result.idpResponse
            if (result.resultCode == RESULT_OK) {

                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser // : FirebaseUser
                //start the MainActivity
                val intent_main_activity = Intent(this, MainActivity::class.java)
                intent_main_activity.putExtra("user", user)
                startActivity(intent_main_activity)

            } else {
                // LOGIN FAILED (toast, redirect to login page again, ...)
                Log.e("FirebaseAuth", "Login ERROR")
            }
        }

    }
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        /*
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            //AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.AnonymousBuilder().build()
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setIsSmartLockEnabled(false)
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
         */
    }
}