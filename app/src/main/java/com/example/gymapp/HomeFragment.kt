package com.example.gymapp

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import com.example.gymapp.databinding.FragmentHomeBinding
import com.example.gymapp.login.WelcomeActivity
import com.firebase.ui.auth.AuthUI
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropSquareTransformation
import java.util.Calendar


class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var binding : FragmentHomeBinding
    private val viewModel: GymViewModel by activityViewModels()
    private lateinit var picker : MaterialTimePicker
    private lateinit var calendar : Calendar
    private var alarmManager : AlarmManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        /* SHOW USER INFO */
        showUserData()

        /* SHOW USER's TRAINING PLANS */
        // TODO => similar to previous observer


        /* BUTTONS */
        binding.homeBtnSignOut.setOnClickListener {
            AuthUI.getInstance()
                .signOut(requireContext())
                .addOnCompleteListener {
                    val intent = Intent(requireContext(), WelcomeActivity::class.java)
                    startActivity(intent)
                }
        }

        binding.homeBtnTrainingReminder.setOnClickListener {
            showTimerPicker()
        }

        /* NAVIGATION */
        val navController = Navigation.findNavController(view)
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

    /**
     *
     */
    private fun showUserData() {
        viewModel.userPersonalData.observe(viewLifecycleOwner, Observer {
            val customMsg =
                if ((viewModel.userPersonalData.value?.username) != "")
                    viewModel.userPersonalData.value?.username
                else if (viewModel.userPersonalData.value?.name != "")
                    viewModel.userPersonalData.value?.name
                else ""
            val txtWelcome = when (customMsg) {
                "" -> resources.getString(R.string.welcome_message)
                else -> resources.getString(R.string.welcome_message_custom, customMsg)
            }
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

        viewModel.userPhotoUrl.observe(viewLifecycleOwner) { uri ->
            val rotationDegrees = if (viewModel.isImageRotated) 0F else 90F
            Picasso.get().load(uri)
                .transform(CropSquareTransformation())
                .fit().centerInside()
                .rotate(rotationDegrees)
                .error(R.drawable.charles_leclerc)
                .placeholder(R.drawable.avatar_default)
                .into(binding.imgProfile)
        }
    }

    /**
     *
     */
    private fun showTimerPicker() {
        calendar = Calendar.getInstance()
        picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendar.get(Calendar.HOUR))
            .setMinute(calendar.get(Calendar.MINUTE))
            .setTitleText("Select alarm time")
            .build()
        picker.show(parentFragmentManager, "gym_app")
        /* picker.addOnPositiveButtonClickListener {
            Log.d(MainActivity.TAG, "OnPositive")
        } */
        picker.addOnPositiveButtonClickListener {
            setAlarm()
        }
    }

    private fun setAlarm() {
        calendar = Calendar.getInstance()
        val intent = Intent(requireContext(), AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager = requireContext().getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager!!.setRepeating(
            AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY, pendingIntent
        )
        Toast.makeText(requireContext(), "Alarm set successfully", Toast.LENGTH_SHORT).show()

        // TODO => show time set (where do u set alarm time!!)
        binding.homeBtnTrainingReminder.text = "(${calendar.get(Calendar.HOUR)}:${calendar.get(Calendar.MINUTE)}) CANCEL ${calendar.time}}"
    }
}