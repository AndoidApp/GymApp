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

/**
 * Alarm status enumeration
 */
enum class AlarmStatus(val intValue: Int) {
    SET(1),
    NOT_SET(0);

    companion object {
        fun fromInt(value: Int?): AlarmStatus {
            return entries.find { it.intValue == value } ?: NOT_SET
        }
    }
}

/**
 * Class to handle alarms storing status and set time in millis
 */
data class AlarmInfo(val status: AlarmStatus, val timeInMillis: Long = DEFAULT_TIME_IN_MILLIS) {

    companion object {
        const val DEFAULT_TIME_IN_MILLIS = -1L
        const val INFO_TO_STORE_IN_FILE = 2
        const val CONTENT_SEPARATOR = ";"
    }

    /**
     * This function returns the string to save alarm info to file
     */
    fun toFile(): String {
        return status.intValue.toString() + CONTENT_SEPARATOR + timeInMillis.toString()
    }
}


/**
 * GymViewModel subclass of [ViewModel].
 * This viewmodel is used to store current user's personal data information and his/her training plans
 */
class GymViewModel : ViewModel() {

    companion object {
        const val TAG = "GymViewModel"
    }

    /** DB */
    private val db: FirebaseFirestore = Firebase.firestore
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    /** LIVE DATA | PERSONAL DATA */
    private val _userPersonalData = MutableLiveData<DBPersonalData>()
    val userPersonalData : LiveData<DBPersonalData>
        get() = _userPersonalData

    private val _userPhotoUrl = MutableLiveData<Uri>()
    val userPhotoUrl : LiveData<Uri>
        get() = _userPhotoUrl

    /** LIVE DATA | TRAINING DATA */
    private val _trainingData = MutableLiveData<DBTrainingPlan>()
    private var _training_Data_Document : MutableLiveData<List<String>> = MutableLiveData()
    var trainingPlanContainer : MutableList<TextView> = mutableListOf()
    var trainingPlanId = -1

    val trainingData : LiveData<DBTrainingPlan>
        get() = _trainingData

    val training_Data_Document : LiveData<List<String>>
        get() = _training_Data_Document


    /* UTILS */
    var isImageRotated = false // avoid profile picture double rotation
    var viewTraining : Boolean = true;
    var alarmInfo = AlarmInfo(AlarmStatus.NOT_SET)

    init {
        // Load personal data from DB
        if (firebaseAuth.currentUser != null) {
            db.collection(firebaseAuth.currentUser!!.uid)
                .get()
                .addOnSuccessListener { result ->
                    var personalData = DBPersonalData()
                    for (document in result) {
                        if (document.id == DBManager.PERSONAL_DATA_DOCUMENT_NAME) {
                            personalData = document.toObject(DBPersonalData::class.java)
                            Log.d(TAG, "${document.id} => ${document.data}")
                            _userPersonalData.value = personalData
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "ERROR", e)
                }
        }
        _userPhotoUrl.value = firebaseAuth.currentUser?.photoUrl
    }

    fun updatePersonalData(newData: DBPersonalData) {
        _userPersonalData.value = newData
    }

    fun updatePhotoUrl(newUrl: Uri) {
        _userPhotoUrl.value = newUrl
        isImageRotated = true // necessary to avoid the image is double rotated in HomeFragment having updated it
    }

    /**
     * Extract all training plans
     */
    fun extractDocument(actionAfterExtraction: () -> Unit) {
        if (firebaseAuth.currentUser != null) {
            db.collection(firebaseAuth.currentUser!!.uid)
                .get()
                .addOnSuccessListener { documents ->
                    val data : MutableList<String> = mutableListOf()

                    // Get training plans
                    for (document in documents) {
                        if (document.id != DBManager.PERSONAL_DATA_DOCUMENT_NAME) {
                            data.add(document.id)
                        }
                    }
                    _training_Data_Document.value = data
                    // Function to execute after data extraction
                    actionAfterExtraction()
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "ERROR", exception)
                }
        }
    }

    /**
     * Extract single training plan
     */
    fun extractDataTraining(actionAfterExtraction: () -> Unit){
        if(_training_Data_Document.value == null)
            return
        if (firebaseAuth.currentUser != null && trainingPlanId != -1 && _training_Data_Document.value!!.size > trainingPlanId) {

            db.collection(firebaseAuth.currentUser!!.uid)
                .document(_training_Data_Document.value!![trainingPlanId])
                .get()
                .addOnSuccessListener { document ->
                    val data = document.data
                    val trainingData = DBTrainingPlan()

                    // Get the training plan data
                    trainingData.exercise = data?.get("Exercise") as MutableList<String>
                    trainingData.set_number = data["Set"] as MutableList<Int>
                    trainingData.reps = data["Reps"] as MutableList<Int>
                    trainingData.weight = data["Weight"] as MutableList<Int>

                    _trainingData.value = trainingData

                    // Function to execute after data extraction
                    actionAfterExtraction()
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "ERROR : $exception")
                }
        }
    }
}
