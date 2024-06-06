package com.example.gymapp

enum class Sex(val displayName: String) {
    MALE("MALE"), FEMALE("FEMALE")
}

data class DBPersonalData(val name: String = DEFAULT_NAME,
                          val username: String = DEFAULT_USERNAME,
                          val dateBirth: String = DEFAULT_BIRTHDATE,
                          val sex: Sex = DEFAULT_SEX) {

    /* DEFAULT VALUES */
    companion object {
        const val DEFAULT_NAME = ""
        const val DEFAULT_USERNAME = ""
        const val DEFAULT_BIRTHDATE = ""
        val DEFAULT_SEX = Sex.MALE
    }

    fun getHashMap(): HashMap<String, String> {
        return hashMapOf(
            "name" to name,
            "username" to username,
            "dateBirth" to dateBirth,
            "sex" to sex.displayName
        )
    }
}

class DBManager {
    companion object {
        const val PERSONAL_DATA_DOCUMENT_NAME = "personal_data"
    }

}