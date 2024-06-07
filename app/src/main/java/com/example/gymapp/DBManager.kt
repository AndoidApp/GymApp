package com.example.gymapp

enum class Sex(val displayName: String) {
    MALE("male"), FEMALE("female")
}

data class DBPersonalData(val name: String, val surname: String, val birthDate: String, val sex: Sex) {
    fun getHashMap(): HashMap<String, String> {
        return hashMapOf(
            "name" to name,
            "surname" to surname,
            "dateBirth" to birthDate,
            "sex" to sex.displayName
        )
    }
}

data class DBTrainingPlan(var exercise: MutableList<String> = mutableListOf(), var set_number: MutableList<Int> = mutableListOf(),
                          var reps: MutableList<Int> = mutableListOf(), var weight: MutableList<Int> = mutableListOf()){
    fun getHashMapTraining(): HashMap<String, List<Any>> {
        return hashMapOf(
        "Exercise" to exercise,
        "Set" to set_number,
        "Reps" to reps,
        "Weight" to weight)
    }
}

class DBManager {

    companion object {
        const val PERSONAL_DATA_DOCUMENT_NAME = "personal_data"
        const val TRAINING_DATA_DOCUMENT_NAME = "training Plans"
    }

}