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

// TODO: Crea una clase TasksActivityAdapter que actúe como adaptador del RecyclerView
//  y que trabaje con una lista de tareas.
//  Cuando se haga click sobre un elemento se debe cambiar el estado de completitud
//  de la tarea, pasando de completada a pendiente o viceversa.
//  La barra de cada elemento tiene un color distinto dependiendo de si la tarea está
//  completada o no.
//  Debajo del concepto se muestra cuando fue creada la tarea, si la tarea está pendiente,
//  o cuando fue completada si la tarea ya ha sido completada.
//  Si la tarea está completada, el checkBox estará chequeado y el concepto estará tachado.

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
                chkCompleted.isChecked = !chkCompleted.isChecked
                onCheckListener?.invoke(adapterPosition)
            }
            chkCompleted.setOnClickListener {
                onCheckListener?.invoke(adapterPosition)
            }
        }

        fun bind(task: Task) {
            task.run {
                containerView.lblConcept.text = getConcept()
                containerView.chkCompleted.isChecked = isCompleted()
                containerView.lblCompleted.text = getText()
                if (isCompleted()) {
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
            return oldItem.getId() == newItem.getId()
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.getText() == newItem.getText() && oldItem.isCompleted() == newItem.isCompleted()
        }

    }
}

