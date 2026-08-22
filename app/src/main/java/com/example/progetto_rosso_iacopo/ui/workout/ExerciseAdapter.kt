package com.example.progetto_rosso_iacopo.ui.workout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.progetto_rosso_iacopo.R
import com.example.progetto_rosso_iacopo.data.model.Exercise

class ExerciseAdapter : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    private var exerciseList: List<Exercise> = emptyList()

    fun submitList(newList: List<Exercise>) {
        exerciseList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exerciseList[position]
        holder.bind(exercise)
    }

    override fun getItemCount(): Int = exerciseList.size

    class ExerciseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(exercise: Exercise) {
            text1.text = exercise.name
            text2.text = "${exercise.sets}x${exercise.reps} - Recupero: ${exercise.restTimeSeconds}s"
        }
    }
}