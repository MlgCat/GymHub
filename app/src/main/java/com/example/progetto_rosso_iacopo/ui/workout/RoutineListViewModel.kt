package com.example.progetto_rosso_iacopo.ui.workout
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.auth
import androidx.lifecycle.ViewModel
import com.example.progetto_rosso_iacopo.data.model.WorkoutRoutine
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.progetto_rosso_iacopo.utils.FetchResult
import com.example.progetto_rosso_iacopo.utils.fetch

class RoutineListViewModel: ViewModel() {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid
    private val _routines: MutableLiveData<List<WorkoutRoutine>> = MutableLiveData()
    val routines: LiveData<List<WorkoutRoutine>> = _routines

    private val _result: MutableLiveData<FetchResult> = MutableLiveData()
    val result: LiveData<FetchResult> = _result

    fun fetchPersonalRoutines(){
        val routinesRef = db.collection("workoutroutines")
        routinesRef.whereEqualTo("creatorId", currentUserId).fetch(
            {querySnapShot-> _routines.value = querySnapShot.toObjects(WorkoutRoutine::class.java)},
            _result)
    }

    fun fetchPublicRoutines(){
        val routinesRef = db.collection("workoutroutines")
        routinesRef.whereEqualTo("public", true).fetch(
            {querySnapShot-> _routines.value = querySnapShot.toObjects(WorkoutRoutine::class.java)},
            _result)
    }

    fun deleteRoutine(routine: WorkoutRoutine){
        val currentList = routines.value?:emptyList()
        if(routine in currentList){
            val newList = currentList.toMutableList()
            newList.remove(routine)
            _routines.value = newList
        }
        db.collection("workoutroutines").document(routine.id)
            .delete()
            .addOnSuccessListener { _result.value = FetchResult.Success }
            .addOnFailureListener { e-> _result.value = FetchResult.FirebaseError(e.localizedMessage) }
    }
}

