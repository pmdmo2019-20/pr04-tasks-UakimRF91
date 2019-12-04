package es.iessaladillo.pedrojoya.pr04.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import es.iessaladillo.pedrojoya.pr04.R
import es.iessaladillo.pedrojoya.pr04.data.entity.Task
import es.iessaladillo.pedrojoya.pr04.utils.strikeThrough
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.tasks_activity_item.*
import kotlinx.android.synthetic.main.tasks_activity_item.view.*

class TasksActivityAdapter : ListAdapter<Task, TasksActivityAdapter.ViewHolder>(TaskDiffCallback) {

    var onCheckListener: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = layoutInflater.inflate(R.layout.tasks_activity_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = currentList[position]
        holder.bind(task)
    }

    inner class ViewHolder (override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {
        init {

            clRoot.setOnClickListener {
                onCheckListener?.invoke(adapterPosition)
            }

            chkCompleted.setOnClickListener {
                onCheckListener?.invoke(adapterPosition)
            }

        }

        fun bind(task: Task) {
            task.run {
                containerView.lblConcept.text = concept
                containerView.chkCompleted.isChecked = completed
                containerView.lblCompleted.text = getText()
                if (completed) {
                    lblConcept.strikeThrough(true)
                    viewBar.setBackgroundResource(R.color.colorCompletedTask)
                } else {
                    lblConcept.strikeThrough(false)
                    viewBar.setBackgroundResource(R.color.colorPendingTask)
                }
            }
        }
    }

    object TaskDiffCallback: DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.concept == newItem.concept && oldItem.completed == newItem.completed
        }

    }
}

