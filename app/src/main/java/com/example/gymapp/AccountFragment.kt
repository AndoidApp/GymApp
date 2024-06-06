package com.example.gymapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.example.gymapp.databinding.FragmentAccountBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.util.Calendar


/**
 * A simple [Fragment] subclass.
 * Use the [AccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccountFragment : Fragment() {

    /* DB */
    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    /* UI */
    private lateinit var binding : FragmentAccountBinding
    private val viewModel: GymViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_account, container, false)
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = Firebase.firestore
        firebaseAuth = FirebaseAuth.getInstance()
        val calendar = Calendar.getInstance()


        /* SET current values */
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

            val c = binding.editDateBirth.text.split("-").toTypedArray()
            calendar.set(c[2].toInt(), c[1].toInt() - 1, c[0].toInt())
        })


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
            datePickerDialog.show()
        }


        // TODO => not working if editText is used
        /* binding.editDateBirth.setOnTouchListener { v, event ->
            { calendar code }
            true // the listener has consumed the event
        } */



        /* UPDATE values */
        val navController = Navigation.findNavController(view)
        binding.editAccountBtnSubmit.setOnClickListener {

            val personalDataUpdated = DBPersonalData(
                binding.editName.text.toString(),
                binding.editUsername.text.toString(),
                binding.editDateBirth.text.toString(),
                when (binding.editSex.checkedRadioButtonId) {
                    R.id.editSexMale -> Sex.MALE
                    R.id.editSexFemale -> Sex.FEMALE
                    else -> { Sex.MALE}
                }
            )

            db.collection(firebaseAuth.currentUser!!.uid)
                .document(DBManager.PERSONAL_DATA_DOCUMENT_NAME)
                .set(personalDataUpdated.getHashMap())

            viewModel.updatePersonalData(personalDataUpdated)

            Toast.makeText(view.context, "Account updated!", Toast.LENGTH_LONG).show()
            navController.navigate(R.id.action_accountFragment_to_homeFragment)
        }
    }
}