package com.example.progetto_rosso_iacopo.ui.workout
import android.os.CountDownTimer
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.progetto_rosso_iacopo.data.model.Exercise
import com.example.progetto_rosso_iacopo.data.model.WorkoutRoutine
import com.example.progetto_rosso_iacopo.utils.asBindingProperty
import com.example.progetto_rosso_iacopo.utils.UniversalBindingDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import androidx.databinding.Bindable
import androidx.databinding.PropertyChangeRegistry


class CreateRoutineViewModel : ViewModel()  {
    private val _id: MutableLiveData<String> = MutableLiveData()
    val id: LiveData<String> = _id
    private val _creatorId: MutableLiveData<String> = MutableLiveData()
    val creatorId: LiveData<String> = _creatorId
    private val _creatorName: MutableLiveData<String> = MutableLiveData()
    val creatorName: LiveData<String> = _creatorName
    val _title: MutableLiveData<String> = MutableLiveData()
    private val _description: MutableLiveData<String> = MutableLiveData()
    var descriptionBinding by _description.asBindingProperty()
    val description : LiveData<String> = _description

    private val _exerciseList: MutableLiveData<List<Exercise>> = MutableLiveData()
    val exerciseList: LiveData<List<Exercise>> = _exerciseList

    val _currentName: MutableLiveData<String> = MutableLiveData()
    var currentNameBinding by _currentName.asBindingProperty()
    val currentName : LiveData<String> = _currentName

    private val _currentReps: MutableLiveData<Int> = MutableLiveData()
    var currentRepsBinding by _currentReps.asBindingProperty()
    val currentReps : LiveData<Int> = _currentReps

    private val _currentSets: MutableLiveData<Int> = MutableLiveData()
    var currentSetsBinding by _currentSets.asBindingProperty()
    val currentSets : LiveData<Int> = _currentSets

    private val _currentRestTimeSeconds: MutableLiveData<Long> = MutableLiveData()
    var currentRestTimeSecondsBinding by _currentRestTimeSeconds.asBindingProperty()
    val currentRestTimeSeconds : LiveData<Long> = _currentRestTimeSeconds

    private val _currentDescription: MutableLiveData<String> = MutableLiveData()
    var currentDescriptionBinding by _currentName.asBindingProperty()
    val currentDescription : LiveData<String> = _currentDescription
    private val _saveStatus = MutableLiveData<SaveResult>()
    val saveStatus: LiveData<SaveResult> = _saveStatus

    val isRoutinePublic: MutableLiveData<Boolean> = MutableLiveData()

    val editedExerciseNum: MutableLiveData<Int?> = MutableLiveData(null)

    fun addExerciseToList() {
        val name = _currentName.value?.trim()
        if (name.isNullOrBlank()) {
            return
        }
        if (editedExerciseNum.value != null){
            editExercise(editedExerciseNum.value?:0)
            return
        }
        val newExercise: Exercise = Exercise(
            name = name,
            reps = _currentReps.value,
            description = _currentDescription.value,
            sets = _currentSets.value,
            restTimeSeconds = _currentRestTimeSeconds.value
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

    fun saveRoutineToFirebase(){
        val name = _title.value?.trim()
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
        val newDocumentRef = db.collection("workoutroutines").document()
        val id = newDocumentRef.id
        val newRoutine = WorkoutRoutine(
            id=id,
            creatorId=uid,
            creatorName = userName,
            title=title,
            description=description,
            isPublic=isPublic,
            exerciseList = list
            )
        newDocumentRef.set(newRoutine)
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



sealed class SaveResult {
    object Success : SaveResult()
    data class ValidationError(val message: String) : SaveResult()
    data class FirebaseError(val message: String) : SaveResult()
}