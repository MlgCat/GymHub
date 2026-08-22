package com.example.progetto_rosso_iacopo.ui.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.progetto_rosso_iacopo.databinding.FragmentEditWorkoutBinding
import kotlin.getValue

class EditRoutineFragment: Fragment() {
    private val viewModel: EditRoutineViewModel by viewModels()
    private var _binding: FragmentEditWorkoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var exerciseAdapter: ModifiableExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditWorkoutBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnExercise.setOnClickListener {
            viewModel.addExerciseToList()
            binding.invalidateAll()
            binding.etExerciseName.requestFocus()
        }
        exerciseAdapter = ModifiableExerciseAdapter(
            {exercise, exerciseNum->
                viewModel.onEditExercise(exercisePos= exerciseNum, exercise= exercise)
                binding.invalidateAll()
                binding.etExerciseName.setText(exercise.name)
            },
            {exerciseNum->
                viewModel.deleteExercise(exerciseNum)
            }
        )
        binding.rvExercises.adapter = exerciseAdapter

        viewModel.exerciseList.observe(viewLifecycleOwner) { list ->
            exerciseAdapter.submitList(list)
        }

        viewModel.editedExerciseNum.observe(viewLifecycleOwner) {
                pos-> exerciseAdapter.setHighlightedPosition(pos)
            if(pos!=null){
                binding.btnExercise.setText("Modifica esercizio")
            }else{
                binding.btnExercise.setText("Aggiungi esercizio")
            }
        }
        viewModel.saveStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is SaveResult.Success -> {
                    Toast.makeText(context, "Scheda salvata con successo!", Toast.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
                is SaveResult.ValidationError -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
                is SaveResult.FirebaseError -> {
                    Toast.makeText(context, "Errore DB: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        val routineId = arguments?.getString("routineId")?:""
        viewModel.loadRoutine(routineId)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}