package com.example.gymapp

enum class Sex(val displayName: String) {
    MALE("MALE"), FEMALE("FEMALE")
}

data class DBPersonalData(val name: String = DEFAULT_NAME,
                          val username: String = DEFAULT_USERNAME,
                          val birthDate: String = DEFAULT_BIRTHDATE,
                          val sex: Sex = DEFAULT_SEX) {

    /* DEFAULT VALUES */
    companion object {
        const val DEFAULT_NAME = ""
        const val DEFAULT_USERNAME = ""
        const val DEFAULT_BIRTHDATE = "1970-1-1"
        val DEFAULT_SEX = Sex.MALE
    }

    fun getHashMap(): HashMap<String, String> {
        return hashMapOf(
            "name" to name,
            "username" to username,
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