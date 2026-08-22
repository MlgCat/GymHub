package com.example.progetto_rosso_iacopo.ui.dashboard
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
import androidx.lifecycle.ViewModel
import com.example.progetto_rosso_iacopo.R
import com.example.progetto_rosso_iacopo.databinding.FragmentDashboardBinding
import com.example.progetto_rosso_iacopo.ui.workout.CreateRoutineViewModel


class DashboardFragment : Fragment() {
    private val viewModel: DashboardViewModel by viewModels()
    var _binding: FragmentDashboardBinding? = null
    val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel=viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabAddWorkout.setOnClickListener {
            // Naviga verso la schermata di aggiunta allenamento usando l'ID dell'azione definita nel nav_graph.xml
            findNavController().navigate(R.id.action_dashboardFragment_to_addWorkoutFragment)
        }
        binding.btnPersonalRoutines.setOnClickListener { findNavController().navigate(R.id.action_dashboardFragment_to_routineListFragment) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Previene i memory leak
    }

}