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
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.example.gymapp.databinding.FragmentTrainingBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

// Class to manage the design and the spawn of graphics element
class DesignManager(var context: Context)
{
    /*
      * Input: - row where to add the TextViews
      *        - number of TextViews to add
      *        - text of the TextViews
      * Output: none
     */
    fun addTextViewsToRow(child : TableRow, numCol : Int, text : List<String> = listOf()){
        for (j in 0 until numCol) {
            // Set layout
            val layoutParams = TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT,
                1f
            )

            val textView = TextView(context)
            // Set text
            textView.text = text[j]
            textView.layoutParams = layoutParams

            // Set row padding
            child.setPadding(10,10,10,10)
            child.addView(textView)
        }
    }

    /*
      * Input: - row where to add the EditText
      *        - number of EditText to add
      *        - boolean to define whether only numeric input is allowed
      * Output: none
     */
    fun addEditTextToRow(child : TableRow, numCol : Int, isNumber : Boolean){
        for (j in 0 until numCol) {
            val editText : EditText = EditText(context)
            if (isNumber)
                editText.inputType = InputType.TYPE_CLASS_NUMBER

            // Set layout
            editText.layoutParams = tableRowDesign()
            child.addView(editText)
        }
    }

    /*
      * Input: - button text
      *        - context
      * Output: button
     */
    fun createButton(text: String, context: Context) : Button{
        val button = Button(context)
        // Set text
        button.text = text

        // Set shape and color
        val background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 100f
            setColor(ContextCompat.getColor(context, R.color.primaryOrange))
        }
        button.background = background

        // Set layout
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )

        layoutParams.setMargins(80, 0, 80, 0)
        button.layoutParams = layoutParams

        return button
    }

    /*
      * Input: none
      * Output: layout param
     */
    fun tableRowDesign() : TableRow.LayoutParams{
        return TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT,
            1f
        )
    }
}

// class to manage the confirmation pop up when the user wants to delete a training plan
class ConfirmationDialogFragment(private val onConfirmAction: () -> Unit) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("Confirm")
                setMessage("Do you really want to delete this training plan? You won't be able to go back")
                setPositiveButton("Confirm",
                    DialogInterface.OnClickListener { dialog, id ->
                        // Function called when the operation is confirmed
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
/*
        trainingData.exercise = viewModel.trainingData.value?.exercise ?: mutableListOf<String>()
        trainingData.set_number = viewModel.trainingData.value?.set_number ?: mutableListOf<Int>()
        trainingData.reps = viewModel.trainingData.value?.reps ?: mutableListOf<Int>()
        trainingData.weight = viewModel.trainingData.value?.weight ?: mutableListOf<Int>()

 */
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTrainingBinding.inflate(layoutInflater)
        return binding.root
    }


    /*
     * input: view
     * output: none
     * note: uses the data taken from DB to visualize the training plan.
     * It also takes care of creating the columns needed to store the weights
     */
    fun viewTraining(view: View)
    {
        val layout : TableLayout = binding.table
        val buttonLayout = binding.buttonLayout
        val navController = Navigation.findNavController(view)
        val manager = DesignManager(requireContext())

        val title = binding.txt
        title.isEnabled = false
        title.setText(viewModel.training_Data_Document.value!![viewModel.trainingPlanId])

        // Create the row [Exercise - Set number - Reps]
        if (viewModel.trainingData.value?.exercise?.size != null) {
            for (i in 0 until viewModel.trainingData.value!!.exercise.size) {
                val row = TableRow(requireContext())
                val textList: List<String> = listOf(
                    viewModel.trainingData.value!!.exercise[i],
                    viewModel.trainingData.value!!.set_number[i].toString(),
                    viewModel.trainingData.value!!.reps[i].toString()
                )
                manager.addTextViewsToRow(row, 3, textList)
                layout.addView(row)
            }
        }

        val row = TableRow(requireContext())
        row.gravity = Gravity.CENTER
        // Create button to save the added weight
        val button = manager.createButton("Save", requireContext())
        // Create button to back home
        val buttonBack = manager.createButton("Home", requireContext())
        // Create button to delete the training plan
        val buttonDelete = manager.createButton("Delete", requireContext())

        row.addView(button)
        row.addView(buttonBack)
        row.addView(buttonDelete)

        buttonLayout.addView(row)

        // Manage the delete button
        buttonDelete.setOnClickListener {
            db = Firebase.firestore
            firebaseAuth = FirebaseAuth.getInstance()

            val confirmationDialog = ConfirmationDialogFragment {
                // If the operation is confirmed, the data is deleted from the database
                db.collection(firebaseAuth.currentUser!!.uid)
                    .document(binding.txt.text.toString())
                    .delete()
                    .addOnCompleteListener {
                        // Set a message to notify the success of the operation
                        Toast.makeText(
                            requireContext(),
                            "Training plan successfully deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                // Back to home
                navController.navigate(R.id.action_trainingFragment_to_homeFragment)
            }
            confirmationDialog.show(childFragmentManager, "confirmation_dialog")
        }

        // Manage back button
        buttonBack.setOnClickListener {
            navController.navigate(R.id.action_trainingFragment_to_homeFragment)
        }

        // Save data in db when Save button is clicked
        button.setOnClickListener {
            if (viewModel.trainingData.value != null) {
                trainingData = viewModel.trainingData.value!!
                // Delete of old data to avoid conflicts
                trainingData.weight.clear()
                // Iterate through all rows
                for (i in DATA_TABLE_ROW_INDEX until layout.childCount) {
                    // Get the row
                    val child: TableRow = layout.getChildAt(i) as TableRow;

                    // Iterate through all weight columns
                    for (j in DATA_TABLE_WEIGHT_INDEX until child.childCount) {
                        // Save the weight if present, otherwise store -1
                        val weightValue: Int =
                            (child.getChildAt(j) as EditText).text.toString().toIntOrNull() ?: -1
                        trainingData.weight.add(weightValue)
                    }
                }
                // Set success msg
                Toast.makeText(
                    requireContext(),
                    "information saved successfully",
                    Toast.LENGTH_SHORT
                ).show()
                // update DB data
                updateTraining(trainingData, binding.txt.text.toString())
            }
        }



        // Calculate how many columns to add to save the weights
        // The max set number represent the columns to add
        var maxValue: Int = 1
        // Iterate through all rows
        for (i in DATA_TABLE_ROW_INDEX until layout.childCount) {
            // Get the row
            val child: TableRow = layout.getChildAt(i) as TableRow
            if (child.getChildAt(DATA_TABLE_SET_INDEX) is TextView) {
                // Get the set number TextView
                val textView: TextView = child.getChildAt(DATA_TABLE_SET_INDEX) as TextView

                // Get the value of TextView
                val textViewValue: Int = textView.text.toString().toIntOrNull() ?: -1
                if (textViewValue > maxValue)
                    // Save the max value
                    maxValue = textView.text.toString().toInt()
            }
        }

        // Create cols to save weight information
        if (layout.getChildAt(DATA_TABLE_SET_INDEX) is TableRow) {
            // Set the cols attribute
            var currRow: TableRow = layout.getChildAt(DATA_TABLE_SET_INDEX) as TableRow;
            manager.addTextViewsToRow(currRow, maxValue, List(maxValue) { "Kg" })

            // Iterate through all rows
            for (i in DATA_TABLE_ROW_INDEX until layout.childCount) {
                currRow = layout.getChildAt(i) as TableRow;
                // Add the edit text to save the weight
                manager.addEditTextToRow(currRow, maxValue, true)
            }
        }

        // Displays the weights entered
        if (viewModel.trainingData.value?.weight?.size != null) {
            var j = 0
            // Iterate through all rows
            for (i in DATA_TABLE_ROW_INDEX until layout.childCount) {
                // Get the row
                var currRow: TableRow = layout.getChildAt(i) as TableRow;
                // Iterate through all weight columns
                for (k in DATA_TABLE_WEIGHT_INDEX until currRow.childCount) {
                    // Get the EditText
                    val editText: EditText = currRow.getChildAt(k) as EditText
                    if (j >= viewModel.trainingData.value!!.weight.size)
                        break
                    // Get the weight value
                    if (viewModel.trainingData.value!!.weight[j] != -1)
                        editText.setText(viewModel.trainingData.value!!.weight[j].toString())
                    j++
                }
            }
        }
    }

    /*
     * input: view
     * output: none
     * note: creates an interface to insert the exercises and saves the values in the DB
     */
    fun createTrainingPlan(view: View)
    {
        val layout : TableLayout = binding.table as TableLayout
        val startRow = TableRow(requireContext())
        val manager = DesignManager(requireContext())

        // Create the first row
        manager.addEditTextToRow(startRow, 1, false)
        manager.addEditTextToRow(startRow, 2, true)
        layout.addView(startRow)

        // Create "New Exercise" and "Submit" button
        val row = TableRow(requireContext())
        val listText : List<String> = listOf("New Ex", "Submit")
        for (element in listText) {
            row.gravity = Gravity.CENTER
            val button = manager.createButton(element, requireContext())
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
            // Clear data to avoid conflict
            trainingData.exercise.clear()
            trainingData.set_number.clear()
            trainingData.reps.clear()
            trainingData.weight.clear()
            // Iterate through all rows
            for (i in DATA_TABLE_ROW_INDEX until layout.childCount) {
                // Get the row
                val child: TableRow = layout.getChildAt(i) as TableRow;

                /*
                   * child.getChildAt(0) = Exercise
                   * child.getChildAt(1) = Set number
                   * child.getChildAt(2) = Reps
                 */
                if (child.getChildAt(0) is EditText && child.getChildAt(1) is EditText && child.getChildAt(2) is EditText
                ) {
                    // Manage missing data
                    if ((child.getChildAt(0) as EditText).text.isEmpty() ||
                        (child.getChildAt(1) as EditText).text.isEmpty() ||
                        (child.getChildAt(2) as EditText).text.isEmpty()
                    ) {
                        // Set msg if there's missing data
                        Toast.makeText(
                            requireContext(),
                            "Missing data in row ${i-1}. The row will be ignored",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Sava the data
                        trainingData.exercise.add(
                            (child.getChildAt(0) as EditText).text.toString()
                        );
                        // Limit the maximum number of series to 6 to avoid graphical problems with the table
                        var setNumber = (child.getChildAt(1) as EditText).text.toString().toInt()
                        if (setNumber > 6) {
                            // set msg
                            Toast.makeText(
                                requireContext(),
                                "The maximum supported set number is six. The entered value will be converted into six",
                                Toast.LENGTH_SHORT
                            ).show()
                            setNumber = 6
                        }
                        trainingData.set_number.add(
                            setNumber
                        );
                        trainingData.reps.add(
                            (child.getChildAt(2) as EditText).text.toString().toInt()
                        )
                    }
                }
            }

            // Update data in DB
            updateTraining(trainingData, binding.txt.text.toString())

            val navController = Navigation.findNavController(view)

            navController.navigate(R.id.action_trainingFragment_to_homeFragment)
        }
    }

    /*
     * input: - Data
     *        - DB document name
     * output: none
     */
    fun updateTraining(TrainingData : DBTrainingPlan, documentName : String){

        db = Firebase.firestore
        firebaseAuth = FirebaseAuth.getInstance()

        db.collection(firebaseAuth.currentUser!!.uid)
            .document(if (documentName != "") documentName else "default name")
            .set(TrainingData.getHashMapTraining())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Create the cols
        val row = TableRow(requireContext())
        val listName = listOf("Exercise", "Set", "Reps")
        val manager = DesignManager(requireContext())

        val background = GradientDrawable().apply {
            setColor(ContextCompat.getColor(requireContext(), R.color.secondary))
        }
        row.background = background

        manager.addTextViewsToRow(row, listName.size, listName)
        binding.table.addView(row)

        // View the training plan or crate a new one
        if (viewModel.viewTraining) {
            viewModel.extractDataTraining() {
                viewTraining(view)
            }
        } else
            createTrainingPlan(view)
    }
}