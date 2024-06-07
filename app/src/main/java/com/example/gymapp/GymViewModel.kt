package com.example.gymapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class GymViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private var db: FirebaseFirestore = Firebase.firestore
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _trainingData = MutableLiveData<DBTrainingPlan>()
    lateinit var userPersonalData: DBPersonalData
    val trainingData : LiveData<DBTrainingPlan>
        get() = _trainingData

    init {
        db.collection(firebaseAuth.currentUser!!.uid)
            .document(DBManager.TRAINING_DATA_DOCUMENT_NAME)
            .get()
            .addOnSuccessListener { document ->
                // [ personal_data , training_plans ]
                val data = document.data

                var trainingData = DBTrainingPlan()

                trainingData.exercise = data?.get("Exercise") as MutableList<String>
                trainingData.set_number = data["Set"] as MutableList<Int>
                trainingData.reps = data["Reps"] as MutableList<Int>
                //trainingData = document.toObject(DBTrainingPlan::class.java)!!
                Log.d("TAG", "${trainingData}")

                _trainingData.value = trainingData
            }
            .addOnFailureListener { exception ->
                Log.e("TAG di errore", "Errore : $exception")
            }
    }

    var viewTraining : Boolean = true;
}

