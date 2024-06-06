package com.example.gymapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.gymapp.databinding.FragmentAccountBinding

/**
 * A simple [Fragment] subclass.
 * Use the [AccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccountFragment : Fragment() {

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

        // SET values
        for (pair in arrayOf(
            binding.editName to viewModel.userPersonalData.value?.name,
            binding.editUsername to viewModel.userPersonalData.value?.username,
            binding.editDateBirth to viewModel.userPersonalData.value?.birthDate
        )) {
            pair.first.setText(pair.second)
        }

        binding.editSex.check(
            if (viewModel.userPersonalData.value?.sex == Sex.MALE)
                R.id.editSexMale
            else R.id.editSexFemale
        )


        // UPDATE values
        val navController = Navigation.findNavController(view)
        binding.editAccountBtnSubmit.setOnClickListener {


            // TODO : actually update account
            // TODO => use date picker instead of date text

            Toast.makeText(view.context, "Account updated!", Toast.LENGTH_LONG).show()
            navController.navigate(R.id.action_accountFragment_to_homeFragment)
        }
    }
}