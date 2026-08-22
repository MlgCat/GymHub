package com.example.progetto_rosso_iacopo.ui.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.LifecycleOwner
import com.example.progetto_rosso_iacopo.databinding.FragmentAddWorkoutBinding
import com.example.progetto_rosso_iacopo.R

class CreateRoutineFragment : Fragment() {

    private val viewModel: CreateRoutineViewModel by viewModels()
    private var _binding: FragmentAddWorkoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var exerciseAdapter: ExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddWorkoutBinding.inflate(inflater, container, false)
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
        exerciseAdapter = ExerciseAdapter()
        binding.rvExercises.adapter = exerciseAdapter

        viewModel.exerciseList.observe(viewLifecycleOwner) { list ->
            exerciseAdapter.submitList(list)
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
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}