package com.example.gymapp.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.gymapp.DBManager
import com.example.gymapp.DBPersonalData
import com.example.gymapp.GymViewModel
import com.example.gymapp.MainActivity
import com.example.gymapp.databinding.ActivityWelcomeBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

/**
 * Activity used to allow user authentication through FirebaseUI Auth
 */
class WelcomeActivity : AppCompatActivity() {

    companion object {
        const val TAG = "FirebaseAuth"
        const val EXTRA_USER_NAME = "user"
    }

    /* DB */
    private val db = Firebase.firestore
    private val firebaseAuth = FirebaseAuth.getInstance()

    private lateinit var binding : ActivityWelcomeBinding
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (firebaseAuth.currentUser == null) {

            binding = ActivityWelcomeBinding.inflate(layoutInflater)
            setContentView(binding.root)

            // Choose authentication providers
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                // TODO AuthUI.IdpConfig.GoogleBuilder().build(),
                AuthUI.IdpConfig.AnonymousBuilder().build()
            )

            // Create and launch sign-in intent
            binding.btnLogin.setOnClickListener{
                val signInIntent = AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setIsSmartLockEnabled(false)
                    .setAvailableProviders(providers)
                    .build()
                signInLauncher.launch(signInIntent)
            }
        } else {
            // User already signed in, load main activity
            startMainActivity()
        }
    }

    override fun onResume() {
        super.onResume()
        if (firebaseAuth.currentUser != null)
            startMainActivity() // User already signed in, load main activity
    }

    /**
     * Handle sign in result
     */
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult?) {
        if (result != null) {
            val response = result.idpResponse
            if (result.resultCode == RESULT_OK) {

                // Successfully signed in
                val user = firebaseAuth.currentUser // : FirebaseUser

                // If the user is a new user, then set default personal data to DB
                if (response?.isNewUser == true) {
                    val defaultPersonalData = DBPersonalData(name = user?.displayName ?: DBPersonalData.DEFAULT_NAME)
                    db.collection(user!!.uid)
                        .document(DBManager.PERSONAL_DATA_DOCUMENT_NAME)
                        .set(defaultPersonalData.getHashMap())
                }

                startMainActivity()

            } else {
                // LOGIN FAILED
                Log.e(TAG, "ERROR | SignIn result")
            }
        }
    }


    /**
     * Start MainActivity putting logged user data as intent extra
     */
    private fun startMainActivity() {
        val intentMainActivity = Intent(this, MainActivity::class.java)
        intentMainActivity.putExtra(EXTRA_USER_NAME, firebaseAuth.currentUser)
        startActivity(intentMainActivity)
    }
}