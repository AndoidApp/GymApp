package com.example.gymapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.example.gymapp.databinding.FragmentHomeBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
        const val TAG = "MainActivity"
    }

    /* DB */
    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var binding : FragmentHomeBinding
    private val viewModel: GymViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = Navigation.findNavController(view)


        /* SHOW USER INFO */
        viewModel.userPersonalData.observe(viewLifecycleOwner, Observer {
            val txtWelcome = resources.getString(R.string.welcome_message,
                if ((viewModel.userPersonalData.value?.username ?: "") != "")
                    viewModel.userPersonalData.value?.username
                else viewModel.userPersonalData.value?.name)

            for (pair in arrayOf(
                binding.homeTxtWelcomeBack to txtWelcome,
                binding.homeTxtName to viewModel.userPersonalData.value?.name,
                binding.homeTxtUsername to viewModel.userPersonalData.value?.username,
                binding.homeTxtBirthDate to viewModel.userPersonalData.value?.dateBirth,
                binding.homeTxtSex to viewModel.userPersonalData.value?.sex?.displayName
            )) {
                pair.first.text = pair.second
            }
        })





        /* SHOW USER's TRAINING PLANS */
        // TODO => similar to previous observer








        /* BUTTONS */
        binding.homeBtnEdit.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_accountFragment)
        }

        binding.Test1.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_trainingFragment)
        }
    }
}