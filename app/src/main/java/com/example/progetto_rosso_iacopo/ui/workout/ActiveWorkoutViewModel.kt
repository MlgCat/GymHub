package com.example.progetto_rosso_iacopo.ui.workout

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
import kotlin.time.Duration.Companion.seconds

class ActiveWorkoutViewModel : ViewModel() {
    private var routine: WorkoutRoutine? = WorkoutRoutine()

    private val _title: MutableLiveData<String> = MutableLiveData("")
    val title: LiveData<String> = _title
    private val _timeLeftSeconds = MutableLiveData<Int>(0)
    val timeLeftSeconds: LiveData<Int> = _timeLeftSeconds

    private val _timerText: MutableLiveData<String> = MutableLiveData(0.asTimerText())
    val timerText: LiveData<String> = _timerText



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

    fun fetchRoutine(id: String) {
        Log.d("FIRESTORE_DEBUG_ACTIVEWORKOUT", "Sto cercando la routine con ID: '$id' cercata da UID:'$userId'")
        val query = db.collection("workoutroutines").document(id)
        query.fetch({ queryWorkout ->
            routine = queryWorkout.toObject(WorkoutRoutine::class.java)
            update(routine)
        }, _result)
    }

    fun update(workoutRoutine: WorkoutRoutine?){
        _title.value = routine?.title
        //_creatorName.value = routine.title
        //_description.value = routine.description
        _exerciseList.value = routine?.exerciseList
        _exerciseNum.value = 0
        _isPreviousButtonEnabled.value = false
        _currentExercise.value = _exerciseList.value?.get(0)?: Exercise()
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
    }

    fun resetTimer(){
        _timeLeftSeconds.value = _currentExercise.value?.restTimeSeconds
        _timerText.value = timeLeftSeconds.value?.asTimerText()
        _isTimerRunning.value = false
        countDownTimer?.cancel()
    }

    fun nextExercise(){
        val last: Int = _exerciseList.value?.lastIndex?: -1
        var current: Int = exerciseNum.value?:0
        if(last > current){
            current = current + 1
            _exerciseNum.value = current
            _isPreviousButtonEnabled.value = (exerciseNum.value?:0) > 0
            _currentExercise.value = routine?.exerciseList[current]?: Exercise()
            updateExercise()
            if (last==current){
                nextButtonText.value = "Fine"
            }
        }
        else{
            _isFinished.value = true
            return
        }
    }

    fun previousExercise() {
        val currentIndex = _exerciseNum.value ?: 0
        if (currentIndex > 0) {
            val prevIndex = currentIndex - 1
            _exerciseNum.value = prevIndex
            _isPreviousButtonEnabled.value = prevIndex>0
            _currentExercise.value = _exerciseList.value?.get(prevIndex)
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
        countDownTimer?.cancel()
    }

    fun Int.asTimerText(): String{
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
            startTimer(timeLeftSeconds.value?:0)
        }
    }
    private var countDownTimer: CountDownTimer? = null
    fun startTimer(seconds: Int){
        countDownTimer?.cancel()
        _isTimerRunning.value = true
        _timeLeftSeconds.value = seconds
        _timerText.value = seconds.asTimerText()
        if(seconds<=0){
            return
        }
        countDownTimer = object : CountDownTimer((seconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeftSeconds.value = (millisUntilFinished / 1000).toInt()
                _timerText.value = timeLeftSeconds.value?.asTimerText()
            }
            override fun onFinish(){
                _isTimerRunning.value= false
                _timeLeftSeconds.value = 0
                _timerText.value = 0.asTimerText()
            }
            }.start()
        }

        override fun onCleared(){
            countDownTimer?.cancel()
        }
}