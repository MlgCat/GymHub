package com.example.progetto_rosso_iacopo.ui.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import com.example.progetto_rosso_iacopo.data.model.WorkoutRoutine
import com.example.progetto_rosso_iacopo.R

class PersonalRoutineAdapter(val onItemClick: (WorkoutRoutine)-> Unit, val onDeleteClick: (WorkoutRoutine)-> Unit, val onEditClick:((WorkoutRoutine)-> Unit)) : RecyclerView.Adapter<PersonalRoutineAdapter.PersonalWorkoutRoutineViewHolder>() {
    private var routineList: List<WorkoutRoutine> = emptyList()
    fun submitList(newList: List<WorkoutRoutine>) {
        routineList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonalWorkoutRoutineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_personal_routine, parent, false)
        return PersonalWorkoutRoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: PersonalWorkoutRoutineViewHolder, position: Int) {
        val routine = routineList[position]
        holder.bind(routine, onItemClick, onEditClick, onDeleteClick)
    }

    override fun getItemCount(): Int = routineList.size

    class PersonalWorkoutRoutineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(R.id.textTitlePersonal)
        private val text2: TextView = itemView.findViewById(R.id.textSubtitlePersonal)
        private val label: LinearLayout = itemView.findViewById(R.id.routineLabel)
        private val deleteButton: View = itemView.findViewById(R.id.btnDelete)
        private val editButton: View = itemView.findViewById(R.id.btnEdit)
        fun bind(routine: WorkoutRoutine, onItemClick: (WorkoutRoutine) -> Unit, onEditClick: (WorkoutRoutine) -> Unit, onDeleteClick: (WorkoutRoutine) -> Unit) {
            text1.text = routine.title
            text2.text = "creato da: ${routine.creatorName}"
            label.setOnClickListener({onItemClick(routine)})
            deleteButton.setOnClickListener({onDeleteClick(routine)})
            editButton.setOnClickListener({onEditClick(routine)})
        }
    }
}