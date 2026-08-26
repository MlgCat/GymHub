package com.example.progetto_rosso_iacopo.ui.workout

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.progetto_rosso_iacopo.data.model.Exercise
import com.example.progetto_rosso_iacopo.data.model.Exercise.Companion.invoke
import com.example.progetto_rosso_iacopo.data.model.WorkoutRoutine
import com.example.progetto_rosso_iacopo.utils.FetchResult
import com.example.progetto_rosso_iacopo.utils.asBindingProperty
import com.example.progetto_rosso_iacopo.utils.fetch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.log

class EditRoutineViewModel: ViewModel() {
    private val _id: MutableLiveData<String> = MutableLiveData()
    val id: LiveData<String> = _id
    private val _creatorId: MutableLiveData<String> = MutableLiveData()
    val creatorId: LiveData<String> = _creatorId
    private val _creatorName: MutableLiveData<String> = MutableLiveData()
    val creatorName: LiveData<String> = _creatorName

    val title: MutableLiveData<String> = MutableLiveData()
    private val _description: MutableLiveData<String> = MutableLiveData()
    var descriptionBinding by _description.asBindingProperty()
    val description: LiveData<String> = _description

    private val _exerciseList: MutableLiveData<List<Exercise>> = MutableLiveData()
    val exerciseList: LiveData<List<Exercise>> = _exerciseList

    val _currentName: MutableLiveData<String> = MutableLiveData()
    var currentNameBinding by _currentName.asBindingProperty()
    val currentName: LiveData<String> = _currentName

    private val _currentReps: MutableLiveData<Int> = MutableLiveData()
    var currentRepsBinding by _currentReps.asBindingProperty()
    val currentReps: LiveData<Int> = _currentReps

    private val _currentSets: MutableLiveData<Int> = MutableLiveData()
    var currentSetsBinding by _currentSets.asBindingProperty()
    val currentSets: LiveData<Int> = _currentSets

    private val _currentRestTimeSeconds: MutableLiveData<Long> = MutableLiveData()
    var currentRestTimeSecondsBinding by _currentRestTimeSeconds.asBindingProperty()
    val currentRestTimeSeconds: LiveData<Long> = _currentRestTimeSeconds

    private val _currentDescription: MutableLiveData<String> = MutableLiveData()
    var currentDescriptionBinding by _currentName.asBindingProperty()
    val currentDescription: LiveData<String> = _currentDescription
    private val _saveStatus = MutableLiveData<SaveResult>()
    val saveStatus: LiveData<SaveResult> = _saveStatus

    val isRoutinePublic: MutableLiveData<Boolean> = MutableLiveData()

    val editedExerciseNum: MutableLiveData<Int?> = MutableLiveData(null)
    private val editedRoutineId: MutableLiveData<String> = MutableLiveData()
    private val editedDocument: MutableLiveData<DocumentReference> = MutableLiveData()

    val _result: MutableLiveData<FetchResult> = MutableLiveData()
    val result: LiveData<FetchResult> = _result

    fun addExerciseToList() {
        val name = _currentName.value?.trim()
        if (name.isNullOrBlank()) {
            return
        }
        if (editedExerciseNum.value != null) {
            editExercise(editedExerciseNum.value ?: 0)
            return
        }
        val newExercise: Exercise = Exercise(
            name = name,
            reps = _currentReps.value?:0,
            description = _currentDescription.value?:"",
            sets = _currentSets.value?:0,
            restTimeSeconds = _currentRestTimeSeconds.value ?: 0L
        )
        val currentList: List<Exercise> = _exerciseList.value ?: emptyList()
        val newList: List<Exercise> = currentList + newExercise
        _exerciseList.value = newList
        resetFields()
    }

    private fun resetFields() {
        currentNameBinding = ""
        currentDescriptionBinding = ""
        currentRepsBinding = ""
        currentSetsBinding = ""
        currentRestTimeSecondsBinding = ""
    }

    fun loadRoutine(routineId: String) {
        editedRoutineId.value = routineId
        val db = FirebaseFirestore.getInstance()
        var workout: WorkoutRoutine?
        val query = db.collection("workoutroutines").document(routineId)
        query.fetch({ queryWorkout ->
            workout = queryWorkout.toObject(WorkoutRoutine::class.java)
            update(workout?: WorkoutRoutine())
        }, _result)
    }

    fun update(routine: WorkoutRoutine){
        _id.value = routine.id
        _creatorId.value = routine.creatorId
        _creatorName.value = routine.creatorName
        title.value = routine.title
        _description.value = routine.description
        _exerciseList.value = routine.exerciseList
    }

    fun saveRoutineToFirebase(){
        val name = title.value?.trim()
        val exerciseList: List<Exercise>? = _exerciseList.value
        if (exerciseList.isNullOrEmpty()){
            _saveStatus.value = SaveResult.ValidationError("Inserisci almeno un esercizio")
            return
        }
        if (name.isNullOrBlank()){
            _saveStatus.value = SaveResult.ValidationError("Inserisci un titolo all'allenamento")
            return
        }
        val title: String = name
        val description: String = _description.value?: ""
        val isPublic: Boolean = isRoutinePublic.value?: false
        val currentUser = FirebaseAuth.getInstance().currentUser
        val uid = currentUser?.uid ?: "unknown_user"
        val userName = currentUser?.displayName ?: "Allenatore Anonimo"
        val list = _exerciseList.value ?: emptyList()

        val db = FirebaseFirestore.getInstance()
        if (editedRoutineId.value == null){
            return
        }
        val documentId: String = editedRoutineId.value?: ""
        val documentRef = db.collection("workoutroutines").document(documentId)
        val newRoutine = WorkoutRoutine(
            id=documentId,
            creatorId=uid,
            creatorName = userName,
            title=title,
            description=description,
            isPublic=isPublic,
            exerciseList = list
        )
        documentRef.set(newRoutine)
            .addOnSuccessListener {
                android.util.Log.d("FIREBASE_DEBUG", "Salvato su Firebase con l'ID di Firestore: $id")
                _saveStatus.value = SaveResult.Success
            }
            .addOnFailureListener { e ->
                android.util.Log.e("FIREBASE_DEBUG", "Errore di salvataggio", e)
                _saveStatus.value = SaveResult.FirebaseError(e.localizedMessage?:"Errore di salvataggio")
            }
    }

    fun deleteExercise(exercisePos: Int) {
        val list: List<Exercise> = exerciseList.value?:emptyList()
        if (exercisePos in list.indices) {
            val newList = list- list[exercisePos]
            _exerciseList.value = newList
            if(exercisePos == (editedExerciseNum.value?:-1)){
                editedExerciseNum.value = null
            }
        }
    }

    fun onEditExercise(exercise: Exercise, exercisePos:Int) {
        currentNameBinding = exercise.name
        currentDescriptionBinding = exercise.description
        currentRepsBinding = exercise.reps.toString()
        currentSetsBinding = exercise.sets.toString()
        currentRestTimeSecondsBinding = exercise.restTimeSeconds.toString()
        editedExerciseNum.value = exercisePos
    }

    fun editExercise(exercisePos: Int){
        val name = _currentName.value?.trim()
        if (name.isNullOrBlank()) {
            return
        }
        val newExercise: Exercise = Exercise(
            name = name,
            reps = _currentReps.value,
            description = _currentDescription.value,
            sets = _currentSets.value,
            restTimeSeconds = _currentRestTimeSeconds.value
        )
        val currentList = _exerciseList.value?.toMutableList() ?: return
        if (exercisePos in currentList.indices) {
            currentList[exercisePos] = newExercise
            _exerciseList.value = currentList
        }
        editedExerciseNum.value = null
        resetFields()
    }
}