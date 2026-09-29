package com.example.progetto_rosso_iacopo.ui.generation

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.ViewModel
import com.example.progetto_rosso_iacopo.data.model.Exercise
import com.example.progetto_rosso_iacopo.data.model.Exercise.Companion.invoke
import com.example.progetto_rosso_iacopo.data.model.WorkoutRoutine
import com.google.firebase.Firebase
import com.google.firebase.functions.functions

class GenerateRoutineViewModel: ViewModel() {
    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    private val goal: MutableLiveData<String> = MutableLiveData()
    private val length: MutableLiveData<Int> = MutableLiveData()
    private val experienceLevel: MutableLiveData<String> = MutableLiveData()
    private val equipment: MutableLiveData<MutableList<String>> = MutableLiveData()
    private val limitations: MutableLiveData<MutableList<String>> = MutableLiveData()
    private val additionalRequests: MutableLiveData<String> = MutableLiveData()
    private val user = FirebaseAuth.getInstance().currentUser

    fun updateGoal(inGoal: String){
        goal.value = inGoal
    }
    fun updateExperienceLevel(inExperienceLevel: String){
        experienceLevel.value = inExperienceLevel
    }

    fun updateLimitations(inLimitations: MutableList<String>){
        limitations.value = inLimitations
    }

    fun updateEquipment(inEquipment: MutableList<String>){
        equipment.value = inEquipment
    }

    fun updateAdditionalRequests(inAdditionalRequests: String){
        additionalRequests.value = inAdditionalRequests
    }

    fun buildRequestOrNull(): WorkoutGenerationRequest?{
        return WorkoutGenerationRequest(
            goal.value?:return null,
            experienceLevel.value?:return null,
            equipment.value?:return null,
            limitations.value?:return null,
            additionalRequests.value?:return null,
        )
    }

    fun WorkoutGenerationRequest.toFunctionData(): Map<String, Any> = mapOf(
        "goal" to goal,
        "experienceLevel" to experienceLevel,
        "equipment" to equipment,
        "limitations" to limitations,
        "additionalRequests" to additionalRequests
    )

    fun onGenerate(){
        val workoutRequest: WorkoutGenerationRequest? = buildRequestOrNull()
        if((workoutRequest == null) ){
            Log.e("RequestError", "richiesta scheda non valida")
            return
        }
        if(user == null){
            Log.e("AuthenticationError", "richiesta scheda da utente non autenticato")
            return
        }
        val functions = Firebase.functions
        functions.getHttpsCallable("generateWorkout")
            .call(workoutRequest.toFunctionData())
            .addOnSuccessListener {
                result->parseWorkout(result.data)
                _isLoading.value = false
            }
            .addOnFailureListener { e->
                Log.d("FunctionsError", e.message?:"errore non gestito")
                _isLoading.value = false
            }
        _isLoading.value = true
    }

    fun parseWorkout(data:Any?): WorkoutRoutine?{
        Log.d("Parsing", "started parsing")
        val isPublic = false
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: "unknown_user"
        val userName = currentUser?.displayName ?: "Allenatore Anonimo"
        val map = data as? Map<String, Any?> ?: return null
        val title: String = map["title"]as? String ?: return null
        Log.d("Parsing", title)
        val rawExercises = map["exerciseList"]as? List<Map<String, Any?>> ?: return null
        val exerciseList: MutableList<Exercise> = mutableListOf<Exercise>()
        for(rawExercise in rawExercises){
            val exercise: Map<String,Any?> = rawExercise as? Map<String, Any?> ?: return null
            val exerciseName: String = exercise["name"] as? String ?: return null
            val reps: Int = exercise["reps"] as? Int?: return null
            val sets: Int = exercise["sets"] as? Int?: return null
            val restTime: Long = exercise["restTime"] as? Long ?: return null
            val description: String = exercise["description"] as? String ?: return null
            val newExercise = Exercise(
                name = exerciseName,
                reps = reps,
                description = description,
                sets = sets,
                restTimeSeconds = restTime,
            )
            exerciseList.add(newExercise)
        }
        return null
    }
}

data class WorkoutGenerationRequest(
    val goal: String,
    val experienceLevel: String,
    val equipment: List<String>,
    val limitations: List<String>,
    val additionalRequests: String
)