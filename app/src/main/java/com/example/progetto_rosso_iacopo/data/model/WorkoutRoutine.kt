package com.example.progetto_rosso_iacopo.data.model

import com.google.firebase.firestore.PropertyName

data class Exercise(
    val name: String = "",
    val reps: Int = 0,
    val sets: Int = 0,
    val restTimeSeconds: Int = 0,
    val description: String = "",
) {
    companion object {
        // L'operatore invoke accetta parametri nullabili
        operator fun invoke(
            name: String,
            reps: Int?,
            sets: Int?,
            restTimeSeconds: Int?,
            description: String?
        ): Exercise {
            return Exercise(
                name = name,
                reps = reps ?: 0,
                sets = sets ?: 0,
                restTimeSeconds = restTimeSeconds ?: 0,
                description = description ?: ""
            )
        }
    }
}

data class WorkoutRoutine(
    val id: String = "",
    val creatorId: String = "",
    val creatorName: String = "",
    val title: String = "",
    val description: String = "",
    val exerciseList: List<Exercise> = emptyList(),
    @get:PropertyName("public")
    @set:PropertyName("public")
    var isPublic: Boolean = false
)
