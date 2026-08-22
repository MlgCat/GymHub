package com.example.progetto_rosso_iacopo.ui.workout

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.progetto_rosso_iacopo.R
import com.example.progetto_rosso_iacopo.data.model.WorkoutRoutine
import com.example.progetto_rosso_iacopo.databinding.FragmentRoutineDetailBinding
import com.example.progetto_rosso_iacopo.utils.FetchResult
import com.example.progetto_rosso_iacopo.utils.fetch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.getValue

class RoutineDetailFragment: Fragment() {
    private val viewModel: RoutineDetailViewModel by viewModels()
    private var _binding: FragmentRoutineDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var exerciseAdapter: ExerciseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRoutineDetailBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exerciseAdapter = ExerciseAdapter()
        binding.rvDetail.adapter = exerciseAdapter

        viewModel.exerciseList.observe(viewLifecycleOwner) { list ->
            exerciseAdapter.submitList(list)
        }
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
        viewModel.fetchRoutine(arguments?.getString("routineId")?:"")
        if(viewModel.creatorId.value != viewModel.userId){
            binding.llButtons.visibility = View.GONE
        }
        binding.btnStartWorkout.setOnClickListener { onStartWorkoutClicked() }
        binding.btnEdit.setOnClickListener { val bundle = Bundle().apply {
                putString("routineId", arguments?.getString("routineId")?:"")
            }
            //findNavController().navigate(R.id.action_routineDetailFragment_to_editFragment, bundle)
        }

        binding.btnDelete.setOnClickListener {
            viewModel.deleteRoutine()
            findNavController().navigateUp()
        }
    }

    fun onStartWorkoutClicked(){
        val bundle = Bundle().apply {
            putString("routineId", arguments?.getString("routineId")?:"")
        }
        findNavController().navigate(R.id.action_routineDetailFragment_to_workoutFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}