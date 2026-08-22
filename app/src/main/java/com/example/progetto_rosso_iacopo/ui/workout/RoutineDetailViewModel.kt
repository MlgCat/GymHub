package com.example.progetto_rosso_iacopo.ui.workout

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.progetto_rosso_iacopo.data.model.Exercise
import com.example.progetto_rosso_iacopo.data.model.WorkoutRoutine
import com.example.progetto_rosso_iacopo.utils.FetchResult
import com.example.progetto_rosso_iacopo.utils.fetch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class RoutineDetailViewModel: ViewModel(){
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val _id: MutableLiveData<String> = MutableLiveData()
    val id: LiveData<String> = _id
    private val _creatorId: MutableLiveData<String> = MutableLiveData()
    val creatorId: LiveData<String> = _creatorId
    private val _creatorName: MutableLiveData<String> = MutableLiveData()
    val creatorName: LiveData<String> = _creatorName
    private val _title: MutableLiveData<String> = MutableLiveData("scheda personalizzata")
    val title: LiveData<String> = _title
    private val _description: MutableLiveData<String> = MutableLiveData()
    val description: LiveData<String> = _description

    private val _exerciseList: MutableLiveData<List<Exercise>> = MutableLiveData()
    val exerciseList: LiveData<List<Exercise>> = _exerciseList

    private val _result: MutableLiveData<FetchResult> = MutableLiveData()
    val result: LiveData<FetchResult> = _result

    val db = FirebaseFirestore.getInstance()

    var workout: WorkoutRoutine? = null

    var workoutDocument: DocumentReference? = null

    fun fetchRoutine(id: String){
        Log.d("FIRESTORE_DEBUG", "Sto cercando la routine con ID: '$id' cercata da UID:'$userId'")
        val query = db.collection("workoutroutines").document(id)
        query.fetch({
                queryWorkout-> workout = queryWorkout.toObject(WorkoutRoutine::class.java)
            update(workout)}, _result)
    }

    fun update(workoutRoutine: WorkoutRoutine?){
        _title.value = workout?.title
        _creatorName.value = workout?.title
        _description.value = workout?.description
        _exerciseList.value = workout?.exerciseList
    }

    fun deleteRoutine(){
        if(workoutDocument == null){
            Log.w("Generic", "Errore durante l'eliminazione")
            return
        }
        workoutDocument?.delete()
            ?.addOnSuccessListener {
                Log.d("Firestore", "Documento eliminato con successo!")
            }
            ?.addOnFailureListener { e ->
                Log.w("Firestore", "Errore durante l'eliminazione", e)
            }
    }
}