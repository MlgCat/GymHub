package com.example.progetto_rosso_iacopo.ui.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.progetto_rosso_iacopo.data.model.WorkoutRoutine
import com.example.progetto_rosso_iacopo.R

class RoutineAdapter(val onItemClick: ((WorkoutRoutine)-> Unit)) : RecyclerView.Adapter<RoutineAdapter.WorkoutRoutineViewHolder>() {
    private var routineList: List<WorkoutRoutine> = emptyList()
    fun submitList(newList: List<WorkoutRoutine>) {
        routineList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutRoutineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_public_routine, parent, false)
        return WorkoutRoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutRoutineViewHolder, position: Int) {
        val routine = routineList[position]
        holder.bind(routine, onItemClick)
    }

    override fun getItemCount(): Int = routineList.size

    class WorkoutRoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(R.id.textTitle)
        private val text2: TextView = itemView.findViewById(R.id.textSubtitle)
        fun bind(routine: WorkoutRoutine, onItemClick: (WorkoutRoutine) -> Unit) {
            text1.text = routine.title
            text2.text = "creato da: ${routine.creatorName}"
            itemView.rootView.setOnClickListener({onItemClick(routine)})
        }
    }
}