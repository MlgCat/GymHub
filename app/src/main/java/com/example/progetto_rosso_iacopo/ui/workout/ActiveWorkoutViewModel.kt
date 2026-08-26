package com.example.progetto_rosso_iacopo.ui.workout

import android.app.Application
import android.content.SharedPreferences
import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.progetto_rosso_iacopo.data.model.Exercise
import com.example.progetto_rosso_iacopo.data.model.WorkoutRoutine
import com.example.progetto_rosso_iacopo.utils.FetchResult
import com.example.progetto_rosso_iacopo.utils.fetch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.lifecycle.AndroidViewModel
import kotlin.time.Duration.Companion.seconds
//ho bisogno dell'application context per salvare lo stato attuale dell'allenamento

class ActiveWorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private var routine: WorkoutRoutine? = WorkoutRoutine()

    private val _title: MutableLiveData<String> = MutableLiveData("")
    val title: LiveData<String> = _title
    private val _timeLeftSeconds = MutableLiveData<Long>(0L)
    val timeLeftSeconds: LiveData<Long> = _timeLeftSeconds

    private val _timerText: MutableLiveData<String> = MutableLiveData(0L.asTimerText())
    val timerText: LiveData<String> = _timerText

    private val _timerEnd: MutableLiveData<Long> = MutableLiveData()

    private val _isTimerRunning = MutableLiveData<Boolean>(false)
    val isTimerRunning: LiveData<Boolean> = _isTimerRunning

    private val _exerciseList: MutableLiveData<List<Exercise>> = MutableLiveData()
    private val _exerciseDescription: MutableLiveData<String> = MutableLiveData("")
    val exerciseDescription: LiveData<String> = _exerciseDescription

    private val _exerciseName: MutableLiveData<String> = MutableLiveData("")
    val exerciseName: LiveData<String> = _exerciseName

    private val _exerciseReps: MutableLiveData<Int> = MutableLiveData(0)
    val exerciseReps: LiveData<Int> = _exerciseReps

    private val _exerciseSets: MutableLiveData<Int> = MutableLiveData(0)
    val exerciseSets: LiveData<Int> = _exerciseSets

    val userId = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    private val _exerciseNum: MutableLiveData<Int> = MutableLiveData()
    val exerciseNum:LiveData<Int> = _exerciseNum

    private val _currentExercise: MutableLiveData<Exercise> = MutableLiveData()
    val currentExercise:LiveData<Exercise> = _currentExercise

    private val _result : MutableLiveData<FetchResult> = MutableLiveData()
    val result: LiveData<FetchResult> = _result

    private val _nextButtonText: MutableLiveData<String> = MutableLiveData("Avanti")
    val nextButtonText: MutableLiveData<String> = _nextButtonText

    private val _isFinished: MutableLiveData<Boolean> = MutableLiveData(false)
    val isFinished: LiveData<Boolean> = _isFinished

    private val _isPreviousButtonEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    val isPreviousButtonEnabled: LiveData<Boolean> = _isPreviousButtonEnabled

    //uso lazy per evitare di chiamare getApplication prima che sia pronto
    private val prefs by lazy {
        application.getSharedPreferences(
            "active-workout-state",
            Context.MODE_PRIVATE
        )
    }

    fun saveWorkoutState(){
        prefs.edit()
            .putInt("exerciseNum", exerciseNum.value?:0)
            .putInt("currentSet", exerciseSets.value?:0)
            .putString("routineId", (routine?.id)?:"")
            .putBoolean("isTimerRunning", (isTimerRunning.value)?:false)
            .putLong("timerEnd", _timerEnd.value?:0).apply()
    }

    fun reloadRoutine(){
        val id = prefs.getString("routineId", "")
        Log.d("Info", "reloading $id")
        if(!id.isNullOrEmpty()) {
            _exerciseNum.value = prefs.getInt("exerciseNum", 0)
            _isPreviousButtonEnabled.value = (exerciseNum.value?:-1) > 0
            val isRunning = prefs.getBoolean("isTimerRunning", false)
            val savedTimerEnd = prefs.getLong("timerEnd", -1)
            //quando ho aggiornato l'esercizio sovrascrivo exerciseSets e timerEnd
            fetchRoutine(id, exerciseNum.value?:0) {
                _exerciseSets.value = prefs.getInt("currentSet", 0)
                _timerEnd.value = savedTimerEnd
                _isTimerRunning.value = isRunning
                if(isRunning){
                    restoreTimerFromSavedEnd(savedTimerEnd)
                }
            }
        }
    }

    fun clearWorkoutState(){
        prefs.edit()
            .putInt("exerciseNum", 0)
            .putInt("currentSet", 0)
            .putString("routineId", "")
            .putLong("timerEnd", 0L)
            .putBoolean("isTimerRunning", false)
            .apply()
    }

    fun fetchRoutine(id: String, exerciseNum: Int = 0, onComplete: (()->Unit)? = null) {
        if(id == ""){
            _result.value = FetchResult.GenericError("tentativo di cercare scheda con id vuoto")
            _isFinished.value = true
            return
        }
        Log.d("FIRESTORE_DEBUG_ACTIVEWORKOUT", "Sto cercando la routine con ID: '$id' cercata da UID:'$userId'")
        val query = db.collection("workoutroutines").document(id)
        query.fetch({ queryWorkout ->
            routine = queryWorkout.toObject(WorkoutRoutine::class.java)
            update(routine, exerciseNum)
            onComplete?.invoke()
        }, _result)
    }
    fun update(workoutRoutine: WorkoutRoutine?, exerciseNum: Int = 0){
        _title.value = routine?.title
        //_creatorName.value = routine.title
        //_description.value = routine.description
        _exerciseList.value = routine?.exerciseList
        _exerciseNum.value = exerciseNum
        _isPreviousButtonEnabled.value = exerciseNum>0
        _currentExercise.value = _exerciseList.value?.getOrNull(exerciseNum)?: Exercise()
        updateExercise()
    }

    fun nextSet(){
        if (exerciseSets.value == null){
            Log.d("FIRESTORE_DEBUG_ACTIVEWORKOUT", "exerciseSets.value not set")
            return
        }
        if(exerciseSets.value!! > 1) {
            _exerciseSets.value = exerciseSets.value!! - 1
        }
        else if(exerciseSets.value!! == 1) {
            nextExercise()
        }
        else if(exerciseSets.value!! < 1){
            Log.d("FIRESTORE_DEBUG_ACTIVEWORKOUT", "exerciseSets.value is negative")
            return
        }
        resetTimer()
        saveWorkoutState()
    }

    fun previousSet(){
        if(exerciseSets.value == null){
            Log.d("FIRESTORE_DEBUG_ACTIVEWORKOUT", "exerciseSets.value not set")
            return
        }
        val maxSets = currentExercise.value?.sets?:1
        if(exerciseSets.value!! < maxSets){
            _exerciseSets.value = exerciseSets.value!! + 1
        }
        resetTimer()
        saveWorkoutState()
    }

    fun resetTimer(){
        _timeLeftSeconds.value = _currentExercise.value?.restTimeSeconds
        _timerText.value = timeLeftSeconds.value?.asTimerText()
        _isTimerRunning.value = false
        prefs.edit().putBoolean("isTimerRunning", false).apply()
        countDownTimer?.cancel()
    }

    fun nextExercise(){
        val last: Int = _exerciseList.value?.lastIndex?: -1
        var current: Int = exerciseNum.value?:0
        if(last > current){
            current = current + 1
            _exerciseNum.value = current
            _isPreviousButtonEnabled.value = (exerciseNum.value?:0) > 0
            _currentExercise.value = routine?.exerciseList?.getOrNull(current)?: Exercise()
            updateExercise()
            if (last==current){
                nextButtonText.value = "Fine"
            }
        }
        else{
            _isFinished.value = true
            clearWorkoutState()
        }
        saveWorkoutState()
    }

    fun previousExercise() {
        val currentIndex = _exerciseNum.value ?: 0
        if (currentIndex > 0) {
            val prevIndex = currentIndex - 1
            _exerciseNum.value = prevIndex
            _isPreviousButtonEnabled.value = prevIndex>0
            _currentExercise.value = _exerciseList.value?.getOrNull(prevIndex)
            nextButtonText.value = "Avanti"
            updateExercise()
        }
    }

    fun updateExercise(){
        _exerciseName.value = currentExercise.value?.name
        _exerciseSets.value = currentExercise.value?.sets
        _exerciseReps.value = currentExercise.value?.reps
        _exerciseDescription.value = currentExercise.value?.description
        _timeLeftSeconds.value = _currentExercise.value?.restTimeSeconds
        _timerText.value = timeLeftSeconds.value?.asTimerText()
        _isTimerRunning.value = false
        prefs.edit().putBoolean("isTimerRunning", false).apply()
        countDownTimer?.cancel()
    }

    fun Long.asTimerText(): String{
        if(this>=60){
            return "${this/60}min ${this%60}s"
        } else if(this<=0){
            return "Fine"
        }
        else{
            return "${this%60}s"
        }
    }

    fun toggleTimer(){
        if(isTimerRunning.value?: false){
            val seconds = _timeLeftSeconds.value ?: 0
            countDownTimer?.onFinish()
            countDownTimer?.cancel()
            _timeLeftSeconds.value = seconds
            _timerText.value = seconds.asTimerText()
        }
        else {
            startTimer(timeLeftSeconds.value?:0L)
        }
    }
    private var countDownTimer: CountDownTimer? = null
    fun startTimer(seconds: Long) {
        countDownTimer?.cancel()
        if (seconds <= 0L) {
            _isTimerRunning.value = false
            prefs.edit().putBoolean("isTimerRunning", false).apply()
            _timeLeftSeconds.value = 0L
            _timerEnd.value = 0L
            _timerText.value = 0L.asTimerText()
            return
        }
        _isTimerRunning.value = true
        _timeLeftSeconds.value = seconds
        val newEnd = System.currentTimeMillis() + (seconds * 1000L)
        _timerEnd.value = newEnd
        prefs.edit()
            .putLong("timerEnd", newEnd)
            .putBoolean("isTimerRunning", true).apply()
        _timerText.value = seconds.asTimerText()
        countDownTimer = object : CountDownTimer(seconds * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val remainingSeconds = millisUntilFinished / 1000L
                _timeLeftSeconds.value = remainingSeconds
                _timerText.value = remainingSeconds.asTimerText()
            }

            override fun onFinish() {
                _isTimerRunning.value = false
                prefs.edit().putBoolean("isTimerRunning", false).apply()
                _timeLeftSeconds.value = 0L
                _timerEnd.value = 0L
                _timerText.value = 0L.asTimerText()
            }
        }.start()
    }

    fun restoreTimerFromSavedEnd(savedTimerEnd: Long) {
        val currentTime = System.currentTimeMillis()
        val remainingMillis = savedTimerEnd - currentTime

        if (remainingMillis > 0) {
            startTimer(remainingMillis / 1000L)
        } else {
            _isTimerRunning.value = false
            prefs.edit().putBoolean("isTimerRunning", true).apply()
            _timeLeftSeconds.value = 0L
            _timerText.value = 0L.asTimerText()
        }
    }

        override fun onCleared(){
            countDownTimer?.cancel()
        }
}