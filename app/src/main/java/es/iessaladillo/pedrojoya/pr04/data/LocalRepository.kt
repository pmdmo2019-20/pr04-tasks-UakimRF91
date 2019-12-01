package es.iessaladillo.pedrojoya.pr04.data

import es.iessaladillo.pedrojoya.pr04.data.entity.Task

object LocalRepository : Repository {
    private var insertions : Long = 0

    private val tasks: MutableList<Task> = ArrayList()

    override fun queryAllTasks(): List<Task> {
        return tasks
    }

    override fun queryCompletedTasks(): List<Task> {
        return tasks.filter { task -> task.isCompleted() }
    }

    override fun queryPendingTasks(): List<Task> {
        return tasks.filter { task -> !task.isCompleted() }
    }

    override fun addTask(concept: String) {
        val now = java.util.Calendar.getInstance().toString()
        tasks.add(Task(insertions + 1, concept, now, false, ""))
    }

    override fun insertTask(task: Task) {
        tasks.add(task)
    }

    override fun deleteTask(taskId: Long) {
        tasks.removeAt(tasks.indexOf(tasks.find { task -> task.getId() == taskId }))
    }

    override fun deleteTasks(taskIdList: List<Long>) {
        taskIdList.forEach {
            tasks.removeAt(tasks.indexOf(tasks.find { task -> task.getId() == it }))
        }
    }

    override fun markTaskAsCompleted(taskId: Long) {
        tasks[tasks.indexOf(tasks.find { task -> task.getId() == taskId })].setCompleted(true);
    }

    override fun markTasksAsCompleted(taskIdList: List<Long>) {
        taskIdList.forEach {
            tasks[tasks.indexOf(tasks.find { task -> task.getId() == it })].setCompleted(true);
        }
    }

    override fun markTaskAsPending(taskId: Long) {
        tasks[tasks.indexOf(tasks.find { task -> task.getId() == taskId })].setCompleted(false)
    }

    override fun markTasksAsPending(taskIdList: List<Long>) {
        taskIdList.forEach {
            tasks[tasks.indexOf(tasks.find { task -> task.getId() == it })].setCompleted(false);
        }
    }

    override fun setCompletedAt(taskId: Long, completedAt: String) {
        tasks[tasks.indexOf(tasks.find { task -> task.getId() == taskId })].setCompletedAt(completedAt)
    }
}
