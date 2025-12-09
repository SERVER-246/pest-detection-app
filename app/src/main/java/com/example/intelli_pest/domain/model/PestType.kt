package com.example.intelli_pest.domain.model

/**
 * Represents the types of pests that can be detected
 */
enum class PestType(val displayName: String, val description: String) {
    ARMYWORM("Armyworm", "Fall armyworm damage on sugarcane leaves"),
    HEALTHY("Healthy", "No pest damage detected - healthy crop"),
    INTERNODE_BORER("Internode Borer", "Damage caused by internode borer"),
    MEALY_BUG("Mealy Bug", "Mealy bug infestation"),
    PINK_BORER("Pink Borer", "Pink borer damage on sugarcane"),
    PORCUPINE_DAMAGE("Porcupine Damage", "Physical damage caused by porcupines"),
    RAT_DAMAGE("Rat Damage", "Damage caused by rats"),
    ROOT_BORER("Root Borer", "Root borer infestation"),
    STALK_BORER("Stalk Borer", "Stalk borer damage"),
    TERMITE("Termite", "Termite infestation damage"),
    TOP_BORER("Top Borer", "Top shoot borer damage");

    companion object {
        fun fromIndex(index: Int): PestType? {
            return entries.getOrNull(index)
        }

        fun fromDisplayName(name: String): PestType? {
            return entries.find { it.displayName.equals(name, ignoreCase = true) }
        }
    }
}



