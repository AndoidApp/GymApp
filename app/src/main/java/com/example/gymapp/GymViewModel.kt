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

enum class AlarmStatus(val intValue: Int) {
    SET(1),
    NOT_SET(0);

    companion object {
        fun fromInt(value: Int?): AlarmStatus {
            return entries.find { it.intValue == value } ?: NOT_SET
        }
    }
}

data class AlarmInfo(val status: AlarmStatus, val timeInMillis: Long = DEFAULT_TIME_IN_MILLIS) {
    fun toFile(): String {
        return status.intValue.toString() + CONTENT_SEPARATOR + timeInMillis.toString()
    }

    companion object {
        const val DEFAULT_TIME_IN_MILLIS = -1L
        const val INFO_TO_STORE_IN_FILE = 2
        const val CONTENT_SEPARATOR = ";"
    }
}

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
    private var _training_Data_Document : MutableLiveData<List<String>> = MutableLiveData()
    var trainingPlanContainer : MutableList<TextView> = mutableListOf()
    var trainingPlanId = -1

    val trainingData : LiveData<DBTrainingPlan>
        get() = _trainingData

    val training_Data_Document : LiveData<List<String>>
        get() = _training_Data_Document

    private val _userPhotoUrl = MutableLiveData<Uri>()
    val userPhotoUrl : LiveData<Uri>
        get() = _userPhotoUrl

    /* UTILS */
    var isImageRotated = false
    var viewTraining : Boolean = true;
    var alarmInfo = AlarmInfo(AlarmStatus.NOT_SET)

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

        //extractDocument()
    }

    fun updatePersonalData(newData: DBPersonalData) {
        _userPersonalData.value = newData
    }

    fun updatePhotoUrl(newUrl: Uri) {
        _userPhotoUrl.value = newUrl
        isImageRotated = true
    }

    fun extractDocument(actionAfterExtraction: () -> Unit){
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
                _training_Data_Document.value = data
                actionAfterExtraction()
            }
            .addOnFailureListener { exception ->
                Log.w("TAG", "Errore nel recupero dei documenti", exception)
            }
    }
    fun extractDataTraining(actionAfterExtraction: () -> Unit){
        if(_training_Data_Document.value == null)
            return
        if (trainingPlanId != -1 && _training_Data_Document.value!!.size > trainingPlanId) {
            Log.d("Nell'if:",  "${trainingPlanId} -> ${DBManager.training_Data_Document.size }")
            db.collection(firebaseAuth.currentUser!!.uid)
                .document(_training_Data_Document.value!![trainingPlanId])
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
                    Log.d("Dati palestra", "$trainingData")
                    actionAfterExtraction()
                }
                .addOnFailureListener { exception ->
                    Log.e("TAG di errore", "Errore : $exception")
                }
        }
    }
}
