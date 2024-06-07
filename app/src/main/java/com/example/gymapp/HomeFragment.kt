package com.example.gymapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.gymapp.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

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
        binding.homeTxtWelcomeBack.text = resources.getString(R.string.welcome_message, viewModel.userPersonalData.name)
        binding.homeTxtName.text = viewModel.userPersonalData.name
        binding.homeTxtSurname.text = viewModel.userPersonalData.surname
        binding.homeTxtBirthDate.text = viewModel.userPersonalData.birthDate
        binding.homeTxtSex.text = viewModel.userPersonalData.sex.displayName



        /* SHOW USER's TRAINING PLANS */





        /* BUTTONS */
        binding.homeBtnEdit.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_accountFragment)
        }

        binding.Test1.setOnClickListener {
            viewModel.viewTraining = true
            navController.navigate(R.id.action_homeFragment_to_trainingFragment)
        }

        binding.btnAdd.setOnClickListener {
            viewModel.viewTraining = false
            navController.navigate(R.id.action_homeFragment_to_trainingFragment)
        }
    }
}