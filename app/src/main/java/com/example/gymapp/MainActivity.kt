package com.example.gymapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gymapp.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


class MainActivity : AppCompatActivity() {

    /* DB */
    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var binding : ActivityMainBinding
    private val viewModel: GymViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        db = Firebase.firestore
        firebaseAuth = FirebaseAuth.getInstance()


        /* TODO : look for a good way to save birthDate: Date */
        val personalData = DBPersonalData("Pippo","Pluto", "2002-11-07", Sex.MALE)
        db.collection(firebaseAuth.currentUser!!.uid)
            .document(DBManager.PERSONAL_DATA_DOCUMENT_NAME)
            .set(personalData.getHashMap())

        /* TODO : look on db for user info
        *   & get personalData: DBPersonalData */
        viewModel.userPersonalData = personalData
    }
}