package com.example.progetto_rosso_iacopo.ui.generation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.ViewModel
import com.example.progetto_rosso_iacopo.data.model.Exercise
import com.example.progetto_rosso_iacopo.data.model.Exercise.Companion.invoke
import com.example.progetto_rosso_iacopo.data.model.WorkoutRoutine
import com.example.progetto_rosso_iacopo.ui.workout.SaveResult
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.functions

class GenerateRoutineViewModel: ViewModel() {

    private val _saveStatus = MutableLiveData<SaveResult>()
    val saveStatus: LiveData<SaveResult> = _saveStatus
    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    private val goal: MutableLiveData<String> = MutableLiveData()
    private val length: MutableLiveData<Int> = MutableLiveData()
    private val experienceLevel: MutableLiveData<String> = MutableLiveData()
    private val equipment: MutableLiveData<MutableList<String>> = MutableLiveData()
    private val limitations: MutableLiveData<MutableList<String>> = MutableLiveData()
    private val additionalRequests: MutableLiveData<String> = MutableLiveData()
    private val user = FirebaseAuth.getInstance().currentUser

    private val _result: WorkoutRoutine? = null
    lateinit var result: WorkoutRoutine


    fun updateGoal(inGoal: String) {
        goal.value = inGoal
    }

    fun updateExperienceLevel(inExperienceLevel: String) {
        experienceLevel.value = inExperienceLevel
    }

    fun updateLimitations(inLimitations: MutableList<String>) {
        limitations.value = inLimitations
    }

    fun updateEquipment(inEquipment: MutableList<String>) {
        equipment.value = inEquipment
    }

    fun updateAdditionalRequests(inAdditionalRequests: String) {
        additionalRequests.value = inAdditionalRequests
    }

    fun buildRequestOrNull(): WorkoutGenerationRequest? {
        return WorkoutGenerationRequest(
            goal.value ?: return null,
            experienceLevel.value ?: return null,
            equipment.value ?: mutableListOf(),
            limitations.value ?: mutableListOf(),
            additionalRequests.value ?: "",
        )
    }

    fun WorkoutGenerationRequest.toFunctionData(): Map<String, Any> = mapOf(
        "goal" to goal,
        "experienceLevel" to experienceLevel,
        "equipment" to equipment,
        "limitations" to limitations,
        "additionalRequests" to additionalRequests
    )

    fun onGenerate() {
        val workoutRequest: WorkoutGenerationRequest? = buildRequestOrNull()
        if ((workoutRequest == null)) {
            Log.e("RequestError", "richiesta scheda non valida")
            return
        }
        if (user == null) {
            Log.e("AuthenticationError", "richiesta scheda da utente non autenticato")
            return
        }
        val functions = Firebase.functions("europe-west1")
        if (true) {
            functions.useEmulator("10.0.2.2", 5001)
        }
        functions.getHttpsCallable("generateWorkout")
            .call(workoutRequest.toFunctionData())
            .addOnSuccessListener { result ->
                parseWorkout(result.data)
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                if (e is FirebaseFunctionsException) {
                    Log.e(
                        "GenWorkout",
                        "code=${e.code} message=${e.message} details=${e.details}",
                        e
                    )
                } else {
                    Log.e("GenWorkout", "errore generico", e)
                }
                _isLoading.value = false
            }
        _isLoading.value = true
    }

    fun parseWorkout(data: Any?): WorkoutRoutine? {
        val isPublic = false
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: "unknown_user"
        val userName = currentUser?.displayName ?: "Allenatore Anonimo"

        val map = data as? Map<String, Any?> ?: return null
        val title = map["name"] as? String ?: return null
        val rawExercises = map["exercises"] as? List<Map<String, Any?>> ?: return null

        val exerciseList = mutableListOf<Exercise>()
        for (rawExercise in rawExercises) {
            val exercise = rawExercise as? Map<String, Any?> ?: return null
            val exerciseName = exercise["name"] as? String ?: return null
            val reps = (exercise["reps"] as? Number)?.toInt() ?: return null
            val sets = (exercise["sets"] as? Number)?.toInt() ?: return null
            val restTime = (exercise["restTime"] as? Number)?.toLong() ?: return null
            val description = exercise["description"] as? String ?: return null

            exerciseList.add(
                Exercise(
                    name = exerciseName,
                    reps = reps,
                    description = description,
                    sets = sets,
                    restTimeSeconds = restTime,
                )
            )
        }
        val db = FirebaseFirestore.getInstance()
        val newDocumentRef = db.collection("workoutroutines").document()
        val id = newDocumentRef.id
        val newRoutine = WorkoutRoutine(
            id = id,
            creatorId = uid,
            creatorName = userName,
            title = title,
            isPublic = false,
            exerciseList = exerciseList
        )
        newDocumentRef.set(newRoutine)
            .addOnSuccessListener {
                android.util.Log.d(
                    "FIREBASE_DEBUG",
                    "Salvato su Firebase con l'ID di Firestore: $id"
                )
                result = newRoutine
                _saveStatus.value = SaveResult.Success
            }
            .addOnFailureListener { e ->
                android.util.Log.e("FIREBASE_DEBUG", "Errore di salvataggio", e)
                _saveStatus.value = SaveResult.FirebaseError(e.localizedMessage ?: "Errore di salvataggio")
                _isLoading.value = false
            }

        return WorkoutRoutine(
            title = title,
            exerciseList = exerciseList,
            isPublic = isPublic,
            creatorId = uid,
            creatorName = userName
        )
    }
}

data class WorkoutGenerationRequest(
    val goal: String,
    val experienceLevel: String,
    val equipment: List<String>,
    val limitations: List<String>,
    val additionalRequests: String
)
