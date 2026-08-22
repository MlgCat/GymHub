package com.example.progetto_rosso_iacopo.ui.workout

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.example.progetto_rosso_iacopo.data.model.Exercise
import com.example.progetto_rosso_iacopo.R
import androidx.core.content.ContextCompat
import android.graphics.Color

class ModifiableExerciseAdapter(val onEditClicked:(exercise: Exercise, exercisePos:Int)->Unit, val onDeleteClicked:(Int)->Unit): RecyclerView.Adapter<ModifiableExerciseAdapter.ModifiableExerciseViewHolder>() {

    private var exerciseList: List<Exercise> = emptyList()
        private var highlightedPosition: Int? = null
        fun submitList(newList: List<Exercise>) {
            exerciseList = newList
            notifyDataSetChanged()
        }

    fun setHighlightedPosition(position: Int?) {
        val previousPosition = highlightedPosition
        highlightedPosition = position

        // Notifica il cambiamento solo per le righe interessate (ottimizza le performance)
        previousPosition?.let { notifyItemChanged(it) }
        position?.let { notifyItemChanged(it) }
    }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ModifiableExerciseViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.view_modifiable_exercise, parent, false)
            return ModifiableExerciseViewHolder(view)
        }

        override fun onBindViewHolder(holder: ModifiableExerciseViewHolder, position: Int) {
            val exercise = exerciseList[position]
            val isHighlighted = position == highlightedPosition
            holder.bind(exercise, position, onEdit= onEditClicked, onDelete= onDeleteClicked, isHighlighted = isHighlighted)
        }

        override fun getItemCount(): Int = exerciseList.size

        class ModifiableExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val text1: TextView = itemView.findViewById(R.id.exerciseName)
            private val text2: TextView = itemView.findViewById(R.id.exerciseRepsAndSets)
            private val btnEdit: View = itemView.findViewById(R.id.btnEdit)
            private val btnDelete: View = itemView.findViewById(R.id.btnDelete)

            fun bind(exercise: Exercise, exercisePos:Int, onDelete: (Int) -> Unit, onEdit: (exercise: Exercise, exercisePos: Int) -> Unit, isHighlighted: Boolean) {
                text1.text = exercise.name
                text2.text = "${exercise.sets}x${exercise.reps} - Recupero: ${exercise.restTimeSeconds}s"
                btnDelete.setOnClickListener { onDelete(exercisePos) }
                btnEdit.setOnClickListener { onEdit(exercise, exercisePos) }
                if (isHighlighted) {
                    val color = ContextCompat.getColor(itemView.rootView.context, R.color.teal_200)
                    itemView.rootView.setBackgroundColor(color)
                }
                else {
                    itemView.rootView.setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }
    }