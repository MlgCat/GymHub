package com.example.progetto_rosso_iacopo.ui.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.Navigator
import com.example.progetto_rosso_iacopo.databinding.FragmentRoutineListBinding
import com.example.progetto_rosso_iacopo.data.model.WorkoutRoutine
import com.example.progetto_rosso_iacopo.R
import com.example.progetto_rosso_iacopo.utils.FetchResult


class RoutineListFragment: Fragment() {
    val viewModel: RoutineListViewModel by viewModels()
    var _binding: FragmentRoutineListBinding? = null
    val binding: FragmentRoutineListBinding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentRoutineListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val routineAdapter = PersonalRoutineAdapter(
            onItemClick = {selectedRoutine-> onRoutineClicked(selectedRoutine)},
            onDeleteClick = {selectedRoutine-> onDeleteClicked(selectedRoutine)},
            onEditClick={selectedRoutine-> onEditClicked(selectedRoutine)}
        )
        binding.rvRoutines.adapter = routineAdapter
        viewModel.routines.observe(viewLifecycleOwner){
            newList->
            routineAdapter.submitList(newList)
        }
        viewModel.result.observe(viewLifecycleOwner){ result->
            when (result) {
                is FetchResult.Success -> {
                    Toast.makeText(context, "Schede di allenamento trovate!", Toast.LENGTH_LONG).show()
                }
                is FetchResult.FirebaseError -> {
                    Toast.makeText(context, "Errore DB: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.fetchPersonalRoutines()
    }

    fun onRoutineClicked(routine: WorkoutRoutine):Unit{
        val bundle = Bundle().apply {
            putString("routineId", routine.id)
        }
        findNavController().navigate(R.id.action_routineListFragment_to_routineDetailFragment, bundle)
    }

    fun onEditClicked(routine: WorkoutRoutine):Unit{
        val bundle = Bundle().apply {
            putString("routineId", routine.id)
        }
        findNavController().navigate(R.id.action_routineListFragment_to_editWorkoutFragment, bundle)
    }

    fun onDeleteClicked(routine: WorkoutRoutine):Unit{
        viewModel.deleteRoutine(routine)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}