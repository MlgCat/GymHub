package com.example.progetto_rosso_iacopo.ui.workout

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.progetto_rosso_iacopo.data.model.WorkoutRoutine
import com.example.progetto_rosso_iacopo.databinding.FragmentWorkoutBinding
import com.example.progetto_rosso_iacopo.utils.FetchResult

class ActiveWorkoutFragment : Fragment(){
    var _binding: FragmentWorkoutBinding? = null
    val binding: FragmentWorkoutBinding get() = _binding!!
    val viewModel: ActiveWorkoutViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentWorkoutBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeResults()
        viewModel.fetchRoutine(arguments?.getString("routineId")?:"")
        binding.btnNext.setOnClickListener { viewModel.nextExercise() }
        binding.btnPrevious.setOnClickListener { viewModel.previousExercise() }
        binding.btnStartTimer.setOnClickListener { viewModel.toggleTimer() }
        binding.btnNextSet.setOnClickListener { viewModel.nextSet() }
        binding.btnPreviousSet.setOnClickListener { viewModel.previousSet() }
        binding.btnResetTimer.setOnClickListener { viewModel.resetTimer() }
        viewModel.isTimerRunning.observe(viewLifecycleOwner){ isTimerRunning->
            if(isTimerRunning){
                binding.btnStartTimer.setText("ferma recupero")
            } else{
                binding.btnStartTimer.setText("avvia recupero")
            }
        }
        viewModel.isFinished.observe(viewLifecycleOwner){ isFinished->
            if(isFinished){
                findNavController().navigateUp()
            }
        }
    }

    fun observeResults(){
        viewModel.result.observe(viewLifecycleOwner){ result->
            when (result) {
                is FetchResult.Success -> {
                    Toast.makeText(context, "Scheda di allenamento trovate!", Toast.LENGTH_LONG).show()
                }
                is FetchResult.FirebaseError -> {
                    Toast.makeText(context, "Errore DB: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}