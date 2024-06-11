package com.example.gymapp

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.example.gymapp.databinding.FragmentAccountBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropSquareTransformation
import java.util.Calendar
import java.util.Date


/**
 * Account fragment to manage customization of personal information (account data)
 */
class AccountFragment : Fragment() {

    /* DB */
    private val db = Firebase.firestore
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val storageReference = FirebaseStorage.getInstance().getReference()

    /* UI */
    private lateinit var binding : FragmentAccountBinding
    private val viewModel: GymViewModel by activityViewModels()

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /* SET current values */
        setUserData()

        /* DATE PICKER HANDLING */
        binding.editDateBirth.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    calendar.set(selectedYear, selectedMonth, selectedDay)
                    binding.editDateBirth.text = resources.getString(R.string.dateBirth,
                        selectedDay.toString(),
                        (selectedMonth + 1).toString(),
                        selectedYear.toString())
                },
                year, month, day
            )
            datePickerDialog.datePicker.maxDate = Date().time // user cannot be born in the future
            datePickerDialog.show()
        }

        /* PROFILE PICTURE HANDLING */
        var imageUri: Uri? = null
        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.data
                if (imageUri != null) {
                    Picasso.get().load(imageUri)
                        .transform(CropSquareTransformation())
                        .fit().centerInside()
                        .rotate(90F)
                        .error(R.drawable.charles_leclerc)
                        .placeholder(R.drawable.avatar_default)
                        .into(binding.editImg)
                }
            }
        }

        binding.editImg.setOnClickListener {
            val openGalleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            resultLauncher.launch(openGalleryIntent)
        }


        /* UPDATE values */
        val navController = Navigation.findNavController(view)
        binding.editAccountBtnSubmit.setOnClickListener {
            updateUserData(imageUri)
            Toast.makeText(view.context, resources.getString(R.string.account_fragment_updated), Toast.LENGTH_LONG).show()
            navController.navigate(R.id.action_accountFragment_to_homeFragment)
        }
    }

    /**
     * Load user data from DB to text views, ... (through Observer method)
     */
    private fun setUserData() {
        viewModel.userPersonalData.observe(viewLifecycleOwner, Observer {
            for (pair in arrayOf(
                binding.editName to viewModel.userPersonalData.value?.name,
                binding.editUsername to viewModel.userPersonalData.value?.username,
                binding.editDateBirth to viewModel.userPersonalData.value?.dateBirth
            )) {
                pair.first.text = pair.second
            }

            binding.editSex.check(
                if (viewModel.userPersonalData.value?.sex == Sex.MALE)
                    R.id.editSexMale
                else R.id.editSexFemale
            )

            val c = binding.editDateBirth.text.split("-").toTypedArray() // 25-06-2024
            if (c.size == 3)
                calendar.set(c[2].toInt(), c[1].toInt() - 1, c[0].toInt())
        })

        viewModel.userPhotoUrl.observe(viewLifecycleOwner) { uri ->
            Picasso.get().load(uri)
                .transform(CropSquareTransformation())
                .fit().centerInside()
                .rotate(90F)
                .error(R.drawable.charles_leclerc)
                .placeholder(R.drawable.avatar_default)
                .into(binding.editImg)
        }
    }

    /**
     * Update new user data on the DB
     */
    private fun updateUserData(imageUri: Uri?) {
        val personalDataUpdated = DBPersonalData(
            binding.editName.text.toString(),
            binding.editUsername.text.toString(),
            binding.editDateBirth.text.toString(),
            when (binding.editSex.checkedRadioButtonId) {
                R.id.editSexMale -> Sex.MALE
                R.id.editSexFemale -> Sex.FEMALE
                else -> { Sex.MALE} // as default
            }
        )

        // Push data to DB
        db.collection(firebaseAuth.currentUser!!.uid)
            .document(DBManager.PERSONAL_DATA_DOCUMENT_NAME)
            .set(personalDataUpdated.getHashMap())
        imageUri?.let { uri ->
            uploadImageToFirebase(uri)
            viewModel.updatePhotoUrl(uri)
        }

        // Push data to viewModel
        viewModel.updatePersonalData(personalDataUpdated)
    }

    /**
    Upload image to firebase storage
     */
    private fun uploadImageToFirebase(imageUri: Uri) {
        val fileRef = storageReference.child("${DBManager.PROFILE_PICS_FOLDER}/${firebaseAuth.currentUser?.uid}.jpg")
        fileRef.putFile(imageUri)
            .addOnSuccessListener {
                // Toast.makeText(requireContext(), "Image uploaded!", Toast.LENGTH_SHORT).show()
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    firebaseAuth.currentUser!!
                        .updateProfile(userProfileChangeRequest {
                            photoUri = Uri.parse(uri.toString())
                        })
                        .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(MainActivity.TAG, "User profile updated.")
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), resources.getString(R.string.account_fragment_image_error), Toast.LENGTH_SHORT).show()
            }
    }
}