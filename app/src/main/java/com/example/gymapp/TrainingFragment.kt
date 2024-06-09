package com.example.gymapp

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
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
            val layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
            )

            val textView = TextView(context)
            textView.text = text[j]
            textView.layoutParams = layoutParams

            child.setPadding(10,10,10,10)
            child.addView(textView)
        }
    }

    fun createEditText(child : TableRow, numCol : Int, isNumber : Boolean){
        for (j in 0 until numCol) {
            val editText : EditText = EditText(context)
            if (isNumber)
                editText.inputType = InputType.TYPE_CLASS_NUMBER

            editText.layoutParams = tableRowDesign()
            child.addView(editText)
        }
    }

    fun createButton(row: TableRow? = null, text: String, context: Context) : Button{
        val button = Button(context)
        button.text = text

        row?.gravity = Gravity.CENTER
        val background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 100f
            setColor(ContextCompat.getColor(context, R.color.primaryOrange))
        }
        button.background = background

        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        layoutParams.setMargins(80, 0, 80, 0)
        button.layoutParams = layoutParams

        return button
    }

    fun tableRowDesign() : TableRow.LayoutParams{
        return TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT,
            1f
        )
    }
}

class ConfirmationDialogFragment(private val onConfirmAction: () -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("Confirm")
                setMessage("Do you really want to delete this training plan? You won't be able to go back")
                setPositiveButton("Confirm",
                    DialogInterface.OnClickListener { dialog, id ->
                        onConfirmAction.invoke()
                    })
                setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                    })
            }
            builder.create()
        } ?: throw IllegalStateException("Invalid activity")
    }
}

class TrainingFragment : Fragment() {
    private val DATA_TABLE_ROW_INDEX = 2
    private val DATA_TABLE_WEIGHT_INDEX = 3
    private val DATA_TABLE_SET_INDEX = 1

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


    fun viewTraining(view: View)
    {
        //viewModel.extractDataTraining()
        val layout : TableLayout = binding.table
        val buttonLayout = binding.buttonLayout
        val navController = Navigation.findNavController(view)
        val manager = DesingManager(requireContext())

        val title = binding.txt
        title.isEnabled = false
        title.setText(viewModel.training_Data_Document.value!![viewModel.trainingPlanId])

        if (viewModel.trainingData.value?.exercise?.size != null) {
            for (i in 0 until viewModel.trainingData.value!!.exercise.size) {
                val row = TableRow(requireContext())
                val textList: List<String> = listOf(
                    viewModel.trainingData.value!!.exercise[i],
                    viewModel.trainingData.value!!.set_number[i].toString(),
                    viewModel.trainingData.value!!.reps[i].toString()
                )
                manager.createTextView(row, 3, textList)
                layout.addView(row)
            }
        }

        // Create button to save the added weight
        val row = TableRow(requireContext())
        val button = manager.createButton(row, "Save", requireContext())
        val buttonBack = manager.createButton(row, "Home", requireContext())
        val buttonDelete = manager.createButton(row, "Delete", requireContext())

        row.addView(button)
        row.addView(buttonBack)
        row.addView(buttonDelete)

        buttonLayout.addView(row)

        buttonDelete.setOnClickListener {

            db = Firebase.firestore
            firebaseAuth = FirebaseAuth.getInstance()

            val confirmationDialog = ConfirmationDialogFragment {

                db.collection(firebaseAuth.currentUser!!.uid)
                    .document(binding.txt.text.toString())
                    .delete()
                    .addOnCompleteListener {
                        Toast.makeText(
                            requireContext(),
                            "Training plan successfully deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                navController.navigate(R.id.action_trainingFragment_to_homeFragment)
            }
            confirmationDialog.show(childFragmentManager, "confirmation_dialog")
        }

        buttonBack.setOnClickListener {
            navController.navigate(R.id.action_trainingFragment_to_homeFragment)
        }

        // Save data in db when Save button is clicked
        button.setOnClickListener {
            if (viewModel.trainingData.value != null) {
                trainingData = viewModel.trainingData.value!!
                trainingData.weight.clear()
                for (i in DATA_TABLE_ROW_INDEX until layout.childCount) {
                    val child: TableRow = layout.getChildAt(i) as TableRow;

                    for (j in DATA_TABLE_WEIGHT_INDEX until child.childCount) {
                        val weightValue: Int =
                            (child.getChildAt(j) as EditText).text.toString().toIntOrNull() ?: -1
                        trainingData.weight.add(weightValue)
                    }
                }
                Toast.makeText(
                    requireContext(),
                    "information saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
                updateTraining(trainingData, binding.txt.text.toString())
            }
        }



        // Calculate how many columns to add to save the weights
        var maxValue: Int = 1
        for (i in DATA_TABLE_ROW_INDEX until layout.childCount) {
            val child: TableRow = layout.getChildAt(i) as TableRow;
            if (child.getChildAt(DATA_TABLE_SET_INDEX) is TextView) {
                val textView: TextView = child.getChildAt(DATA_TABLE_SET_INDEX) as TextView

                val textViewValue: Int = textView.text.toString().toIntOrNull() ?: -1
                if (textViewValue > maxValue)
                    maxValue = textView.text.toString().toInt()
            }
        }

        // Create cols to save weight information
        if (layout.getChildAt(DATA_TABLE_SET_INDEX) is TableRow) {
            var currRow: TableRow = layout.getChildAt(DATA_TABLE_SET_INDEX) as TableRow;
            manager.createTextView(currRow, maxValue, List(maxValue) { "Kg" })

            for (i in DATA_TABLE_ROW_INDEX until layout.childCount) {
                currRow = layout.getChildAt(i) as TableRow;
                manager.createEditText(currRow, maxValue, true)
            }
        }

        // TODO: levare questa porcata gigante e inserire i defaul value nella creazione della scheda.
        // TODO: Problema: l'aggiunta di un nuovo eserizio sovrascrive i dati precedenti e perdo i quelli memorizzati
        // TODO: Soluzione: BOH
        if (viewModel.trainingData.value?.weight?.size != null) {
            var j = 0
            outerLoop@ for (i in DATA_TABLE_ROW_INDEX until layout.childCount) {
                var currRow: TableRow = layout.getChildAt(i) as TableRow;
                for (i in DATA_TABLE_WEIGHT_INDEX until currRow.childCount) {
                    val editText: EditText = currRow.getChildAt(i) as EditText
                    if (j >= viewModel.trainingData.value!!.weight.size)
                        break@outerLoop
                    if (viewModel.trainingData.value!!.weight[j] != -1)
                        editText.setText(viewModel.trainingData.value!!.weight[j].toString())
                    j++
                }
            }
        }
    }

    fun createTrainingPlan(view: View)
    {
        // Add button to Row
        val layout : TableLayout = binding.table as TableLayout
        val startRow = TableRow(requireContext())
        val manager = DesingManager(requireContext())

        manager.createEditText(startRow, 1, false)
        manager.createEditText(startRow, 2, true)
        layout.addView(startRow)

        val row = TableRow(requireContext())
        val listText : List<String> = listOf("New Ex", "Submit")
        for (element in listText) {
            val button = manager.createButton(row, element, requireContext())
            row.addView(button)
        }
        binding.buttonLayout.addView(row)

        // Manage new Ex button
        var button : Button = row.getChildAt(0) as Button
        button.setOnClickListener {
            row.removeAllViews()
            layout.removeView(row)
            createTrainingPlan(view)
        }

        // Manage submit button
        button = row.getChildAt(1) as Button
        button.setOnClickListener {
            trainingData.exercise.clear()
            trainingData.set_number.clear()
            trainingData.reps.clear()
            trainingData.weight.clear()
            for (i in DATA_TABLE_ROW_INDEX until layout.childCount) {
                val child: TableRow = layout.getChildAt(i) as TableRow;

                if (child.getChildAt(0) is EditText && child.getChildAt(1) is EditText && child.getChildAt(2) is EditText
                ) {
                    if ((child.getChildAt(0) as EditText).text.isEmpty() ||
                        (child.getChildAt(1) as EditText).text.isEmpty() ||
                        (child.getChildAt(2) as EditText).text.isEmpty()
                    ) {
                        Toast.makeText(
                            requireContext(),
                            "Missing data in row ${i-1}. The row will be ignored",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        trainingData.exercise.add(
                            (child.getChildAt(0) as EditText).text.toString()
                        );
                        trainingData.set_number.add(
                            (child.getChildAt(1) as EditText).text.toString().toInt()
                        );
                        trainingData.reps.add(
                            (child.getChildAt(2) as EditText).text.toString().toInt()
                        )
                    }
                }
            }

            updateTraining(trainingData, binding.txt.text.toString())

            val navController = Navigation.findNavController(view)

            navController.navigate(R.id.action_trainingFragment_to_homeFragment)
        }
    }

    fun updateTraining(TrainingData : DBTrainingPlan, documentName : String){

        db = Firebase.firestore
        firebaseAuth = FirebaseAuth.getInstance()

        db.collection(firebaseAuth.currentUser!!.uid)
            .document(documentName)
            .set(TrainingData.getHashMapTraining())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val row = TableRow(requireContext())
        val listName = listOf("Exercise", "Set", "Reps")
        val manager = DesingManager(requireContext())

        val background = GradientDrawable().apply {
            setColor(ContextCompat.getColor(requireContext(), R.color.secondary)) // Imposta il colore di riempimento
        }
        row.background = background

        manager.createTextView(row, listName.size, listName)
        binding.table.addView(row)

            if (viewModel.viewTraining) {
                viewModel.extractDataTraining() {
                    viewTraining(view)
                }
            } else
                createTrainingPlan(view)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.trainingData.removeObservers(viewLifecycleOwner)
    }
}