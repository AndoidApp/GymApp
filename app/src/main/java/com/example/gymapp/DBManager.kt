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

class DBManager {

    companion object {
        const val PERSONAL_DATA_DOCUMENT_NAME = "personal_data"
    }

}