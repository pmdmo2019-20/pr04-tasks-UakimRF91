package es.iessaladillo.pedrojoya.pr04.data

import android.annotation.SuppressLint
import android.app.Application
import es.iessaladillo.pedrojoya.pr04.R
import es.iessaladillo.pedrojoya.pr04.data.entity.Task
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

object LocalRepository : Repository {
    private var insertions : Long = 0
    @SuppressLint("ConstantLocale")
    private val sdf = SimpleDateFormat("dd/MM/yyyy, hh:mm:ss", Locale.getDefault())
    var id: Long = 0


    private val tasks: MutableList<Task> = ArrayList()

    override fun queryAllTasks(): List<Task> {
        return tasks.sortedByDescending { task -> task.createdAt }
    }

    override fun queryCompletedTasks(): List<Task> {
        return tasks.filter { task -> task.completed }.sortedByDescending { task -> task.createdAt }
    }

    override fun queryPendingTasks(): List<Task> {
        return tasks.filter { task -> !task.completed }.sortedByDescending { task -> task.createdAt }
    }

    override fun addTask(concept: String) {
        val now = java.util.Calendar.getInstance().toString()
        tasks.add(Task(insertions + 1, concept, now, false, ""))
    }

    override fun insertTask(concept: String, application: Application) {
        var stringCreatedAt: String = application.getString(R.string.tasks_item_createdAt, sdf.format(Date()))
        val task = Task(++id, concept, stringCreatedAt, false, "")
        tasks.add(task)
    }

    override fun deleteTask(taskId: Long) {
        tasks.removeAt(tasks.indexOf(tasks.find { task -> task.id == taskId }))
    }

    override fun deleteTasks(taskIdList: List<Long>) {
        taskIdList.forEach {
            tasks.removeAt(tasks.indexOf(tasks.find { task -> task.id == it }))
        }
    }

    override fun markTaskAsCompleted(taskId: Long, application: Application) {
        val position = tasks.indexOf(tasks.find { task -> task.id == taskId })
        val newTask = tasks[position].copy(completed = true, completedAt = application.getString(R.string.tasks_item_completedAt, sdf.format(Date())))
        tasks[position] = newTask
    }

    override fun markTasksAsCompleted(taskIdList: List<Long>, application: Application) {
        taskIdList.forEach {
            markTaskAsCompleted(it, application)
        }
    }

    override fun markTaskAsPending(taskId: Long) {
        val position = tasks.indexOf(tasks.find { task -> task.id == taskId })
        val newTask = tasks[position].copy(completed = false)
        tasks[position] = newTask
    }

    override fun markTasksAsPending(taskIdList: List<Long>) {
        taskIdList.forEach {
            markTaskAsPending(it)
        }
    }

    override fun setCompletedAt(taskId: Long, completedAt: String) {
        val position = tasks.indexOf(tasks.find { task -> task.id == taskId })
        val newTask = tasks[position].copy(completedAt = completedAt)
        tasks[position] = newTask
    }

    private fun now(): String = sdf.format(Date())
}
