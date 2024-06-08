package com.example.gymapp


import android.net.Uri
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class GymViewModel : ViewModel() {

    /* DB */
    private val db: FirebaseFirestore = Firebase.firestore
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    /* LIVE DATA | PERSONAL DATA */
    private val _userPersonalData = MutableLiveData<DBPersonalData>()
    val userPersonalData : LiveData<DBPersonalData>
        get() = _userPersonalData

    /* LIVE DATA | TRAINING DATA */
    private val _trainingData = MutableLiveData<DBTrainingPlan>()
    var trainingPlanContainer : MutableList<TextView> = mutableListOf()
    var trainingPlanId = -1

    val trainingData : LiveData<DBTrainingPlan>
        get() = _trainingData

    private val _userPhotoUrl = MutableLiveData<Uri>()
    val userPhotoUrl : LiveData<Uri>
        get() = _userPhotoUrl

    /* UTILS */
    var isImageRotated = false
    var viewTraining : Boolean = true;

    init {
        db.collection(firebaseAuth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { result ->
                // [ personal_data , training_plans ]
                var personalData = DBPersonalData()
                for (document in result) {
                    personalData = document.toObject(DBPersonalData::class.java)
                    Log.d(MainActivity.TAG, "${document.id} => ${document.data}")
                }
                _userPersonalData.value = personalData
            }
        _userPhotoUrl.value = firebaseAuth.currentUser?.photoUrl

        extractDocument()

        // TODO => fix se l'utente non ha il documento training plan
        extractDataTraining()
    }

    fun updatePersonalData(newData: DBPersonalData) {
        _userPersonalData.value = newData
    }

    fun updatePhotoUrl(newUrl: Uri) {
        _userPhotoUrl.value = newUrl
        isImageRotated = true
    }

    fun extractDocument(){
        db.collection(firebaseAuth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { documents ->
                val data : MutableList<String> = mutableListOf()
                for (document in documents) {
                    if (document.id != "personal_data") {
                        data.add(document.id)

                    }
                    Log.d("Documenti", "${data}")
                }
                DBManager.training_Data_Document = data
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Errore nel recupero dei documenti", exception)
            }
    }
    fun extractDataTraining(){
        if (trainingPlanId != -1 && DBManager.training_Data_Document.size > trainingPlanId) {
            Log.d("Nell'if:",  "${trainingPlanId} -> ${DBManager.training_Data_Document.size }")
            db.collection(firebaseAuth.currentUser!!.uid)
                .document(DBManager.training_Data_Document[trainingPlanId])
                .get()
                .addOnSuccessListener { document ->
                    // [ personal_data , training_plans ]
                    val data = document.data

                    var trainingData = DBTrainingPlan()

                    trainingData.exercise = data?.get("Exercise") as MutableList<String>
                    trainingData.set_number = data["Set"] as MutableList<Int>
                    trainingData.reps = data["Reps"] as MutableList<Int>
                    trainingData.weight = data["Weight"] as MutableList<Int>
                    //trainingData = document.toObject(DBTrainingPlan::class.java)!!

                    _trainingData.value = trainingData
                    Log.d("TAG", "$trainingData")
                }
                .addOnFailureListener { exception ->
                    Log.e("TAG di errore", "Errore : $exception")
                }
        }
    }
}
