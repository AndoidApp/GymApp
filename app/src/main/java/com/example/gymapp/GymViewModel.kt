package com.example.gymapp

import android.net.Uri
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

    /* DB */
    private var db: FirebaseFirestore = Firebase.firestore
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    /* LIVE DATA */
    private val _userPersonalData = MutableLiveData<DBPersonalData>()
    val userPersonalData : LiveData<DBPersonalData>
        get() = _userPersonalData
    private val _userPhotoUrl = MutableLiveData<Uri>()
    val userPhotoUrl : LiveData<Uri>
        get() = _userPhotoUrl
    var isImageRotated = false

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
    }

    fun updatePersonalData(newData: DBPersonalData) {
        _userPersonalData.value = newData
    }

    fun updatePhotoUrl(newUrl: Uri) {
        _userPhotoUrl.value = newUrl
        isImageRotated = true
    }
}