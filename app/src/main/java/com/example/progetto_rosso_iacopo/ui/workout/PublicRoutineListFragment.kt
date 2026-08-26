package com.example.progetto_rosso_iacopo.ui.workout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.progetto_rosso_iacopo.databinding.FragmentRoutineListBinding
import com.example.progetto_rosso_iacopo.data.model.WorkoutRoutine
import com.example.progetto_rosso_iacopo.R
import com.example.progetto_rosso_iacopo.utils.FetchResult


class PublicRoutineListFragment: Fragment() {
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
        val routineAdapter = RoutineAdapter(
            onItemClick = {selectedRoutine-> onRoutineClicked(selectedRoutine)},
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
                is FetchResult.GenericError -> {
                    Toast.makeText(context, "Errore: ${result.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        viewModel.fetchPublicRoutines()
    }

    fun onRoutineClicked(routine: WorkoutRoutine):Unit{
        val bundle = Bundle().apply {
            putString("routineId", routine.id)
        }
        findNavController().navigate(R.id.action_publicRoutineListFragment_to_routineDetailFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}