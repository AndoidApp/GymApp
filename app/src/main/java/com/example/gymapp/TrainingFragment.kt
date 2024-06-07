package com.example.gymapp

import android.app.ActionBar
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.example.gymapp.databinding.FragmentTrainingBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class DesingManager(var context: Context)
{
    fun createTextView(child : TableRow, numCol : Int, text : List<String> = listOf()){
        for (j in 0 until numCol) {
            val textView = TextView(context)

            textView.text = text[j]
            textView.layoutParams = tableRowDesign()
            child.addView(textView)
        }
    }

    fun createEditText(child : TableRow, numCol : Int){
        for (j in 0 until numCol) {
            val editText : EditText = EditText(context)

            editText.layoutParams = tableRowDesign()
            child.addView(editText)
        }
    }

    fun tableRowDesign() : TableRow.LayoutParams{
        return TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT,
            1f
        )
    }
}

class TrainingFragment : Fragment() {

    private lateinit var binding : FragmentTrainingBinding
    private val viewModel: GymViewModel by activityViewModels()
    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    var trainingData = DBTrainingPlan()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        trainingData.exercise = viewModel.trainingData.value?.exercise ?: mutableListOf<String>()
        trainingData.set_number = viewModel.trainingData.value?.set_number ?: mutableListOf<Int>()
        trainingData.reps = viewModel.trainingData.value?.reps ?: mutableListOf<Int>()
        trainingData.weight = viewModel.trainingData.value?.weight ?: mutableListOf<Int>()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTrainingBinding.inflate(layoutInflater)
        return binding.root
    }


    fun viewTraining()
    {
        var maxValue : Int = 1
        val layout : TableLayout = binding.table as TableLayout
        val manager = DesingManager(requireContext())

        var size = viewModel.trainingData.value?.exercise?.size
        Log.d("TAG0", "${size}")
        if (size == null) size = 1
        for (i in 0 until size){
            val row = TableRow(requireContext())
            val textList : List<String> = listOf(
                viewModel.trainingData.value!!.exercise[i],
                viewModel.trainingData.value!!.set_number[i].toString(),
                viewModel.trainingData.value!!.reps[i].toString()
            )
            manager.createTextView(row, 3, textList)
            layout.addView(row)
        }

        val row = TableRow(requireContext())
        val button = Button(requireContext())
        button.text = "Save"
        row.addView(button)
        layout.addView(row)

        button.setOnClickListener{
            for (i in 2 until layout.childCount-1){
                val child : TableRow = layout.getChildAt(i) as TableRow;
                for (j in 3 until child.childCount){
                    trainingData.weight.add((child.getChildAt(j) as EditText).text.toString().toInt())
                }
            }
            updateTraining(trainingData)
        }


        for (i in 2 until layout.childCount-1){
            val child : TableRow = layout.getChildAt(i) as TableRow;
            val textView : TextView = child.getChildAt(1) as TextView
            //TODO: check if the text view is null
            if (textView.text.toString().toInt() > maxValue)
                maxValue = textView.text.toString().toInt()
        }


        var child : TableRow = layout.getChildAt(1) as TableRow;
        manager.createTextView(child, maxValue,  List(maxValue) { "Kg" })

        for (i in 2 until layout.childCount-1) {
            child = layout.getChildAt(i) as TableRow;
            manager.createEditText(child, maxValue)
        }
    }

    fun editTraining(view: View)
    {
        // Add button to Row
        val layout : TableLayout = binding.table as TableLayout
        val startRow = TableRow(requireContext())
        val manager = DesingManager(requireContext())

        manager.createEditText(startRow, 3)
        layout.addView(startRow)

        val row = TableRow(requireContext())
        val listText : List<String> = listOf("New Ex", "Submit")
        for (i in 0 until 2) {
            val button = Button(requireContext())
            button.text = listText[i]
            row.addView(button)
        }
        layout.addView(row)

        // Manage new Ex button
        var button : Button = row.getChildAt(0) as Button
        button.setOnClickListener {
            val manager = DesingManager(requireContext())
            row.removeAllViews()
            manager.createEditText(row, 3)
            editTraining(view)
        }

        // Manage submit button
        button = row.getChildAt(1) as Button
        button.setOnClickListener {
            for (i in 2 until layout.childCount-1){
                val child : TableRow = layout.getChildAt(i) as TableRow;

                trainingData.exercise.add((child.getChildAt(0) as EditText).text.toString())
                trainingData.set_number.add((child.getChildAt(1) as EditText).text.toString().toInt())
                trainingData.reps.add((child.getChildAt(2) as EditText).text.toString().toInt())
            }

            updateTraining(trainingData)

            val navController = Navigation.findNavController(view)

            navController.navigate(R.id.action_trainingFragment_to_homeFragment)
        }

    }

    fun updateTraining(TrainingData : DBTrainingPlan){

        db = Firebase.firestore
        firebaseAuth = FirebaseAuth.getInstance()

        db.collection(firebaseAuth.currentUser!!.uid)
            .document(DBManager.TRAINING_DATA_DOCUMENT_NAME)
            .set(TrainingData.getHashMapTraining())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (viewModel.viewTraining)
            viewTraining()
        else
            editTraining(view)
    }
}