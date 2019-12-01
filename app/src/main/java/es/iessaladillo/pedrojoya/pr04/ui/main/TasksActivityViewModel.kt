package es.iessaladillo.pedrojoya.pr04.ui.main

import android.app.Application
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import es.iessaladillo.pedrojoya.pr04.R
import es.iessaladillo.pedrojoya.pr04.base.Event
import es.iessaladillo.pedrojoya.pr04.data.Repository
import es.iessaladillo.pedrojoya.pr04.data.entity.Task
import java.text.SimpleDateFormat
import java.util.*

class TasksActivityViewModel(private val repository: Repository,
                             private val application: Application) : ViewModel() {

    private val sdf = SimpleDateFormat("dd/MM/yyyy, hh:mm:ss", Locale.getDefault())

    // Estado de la interfaz

    private val _tasks: MutableLiveData<List<Task>> = MutableLiveData()
    val tasks: LiveData<List<Task>>
        get() = _tasks

    private val _currentFilter: MutableLiveData<TasksActivityFilter> =
        MutableLiveData(TasksActivityFilter.ALL)
    val currentFilter: LiveData<TasksActivityFilter>
        get() = _currentFilter

    private val _currentFilterMenuItemId: MutableLiveData<Int> =
        MutableLiveData(R.id.mnuFilterAll)
    val currentFilterMenuItemId: LiveData<Int>
        get() = _currentFilterMenuItemId

    private val _activityTitle: MutableLiveData<String> =
        MutableLiveData(application.getString(R.string.tasks_title_all))
    val activityTitle: LiveData<String>
        get() = _activityTitle

    private val _lblEmptyViewText: MutableLiveData<String> =
        MutableLiveData(application.getString(R.string.tasks_no_tasks_yet))
    val lblEmptyViewText: LiveData<String>
        get() = _lblEmptyViewText

    // Eventos de comunicación con la actividad

    private val _onStartActivity: MutableLiveData<Event<Intent>> = MutableLiveData()
    val onStartActivity: LiveData<Event<Intent>>
        get() = _onStartActivity

    private val _onShowMessage: MutableLiveData<Event<String>> = MutableLiveData()
    val onShowMessage: LiveData<Event<String>>
        get() = _onShowMessage

    private val _onShowTaskDeleted: MutableLiveData<Event<Task>> = MutableLiveData()
    val onShowTaskDeleted: LiveData<Event<Task>>
        get() = _onShowTaskDeleted

    // ACTION METHODS

    // Hace que se muestre en el RecyclerView todas las tareas.
    fun filterAll() {
        _currentFilter.value = TasksActivityFilter.ALL
        _currentFilterMenuItemId.value = R.id.mnuFilterAll
        queryTasks(_currentFilter.value!!)

    }

    // Hace que se muestre en el RecyclerView sólo las tareas completadas.
    fun filterCompleted() {
        _currentFilter.value = TasksActivityFilter.COMPLETED
        _currentFilterMenuItemId.value = R.id.mnuFilterCompleted
        queryTasks(_currentFilter.value!!)
    }

    // Hace que se muestre en el RecyclerView sólo las tareas pendientes.
    fun filterPending() {
        _currentFilter.value = TasksActivityFilter.PENDING
        _currentFilterMenuItemId.value = R.id.mnuFilterPending
        queryTasks(_currentFilter.value!!)
    }

    // Agrega una nueva tarea con dicho concepto. Si la se estaba mostrando
    // la lista de solo las tareas completadas, una vez agregada se debe
    // mostrar en el RecyclerView la lista con todas las tareas, no sólo
    // las completadas.
    fun addTask(concept: String) {
        if (isValidConcept(concept)) {
            insertTask(Task(concept, application.getString(R.string.tasks_item_createdAt, sdf.format(Date())), false, ""))
        } else {
            _onShowMessage.value = Event(application.getString(R.string.tasks_not_valid_concept))
        }

    }

    // Agrega la tarea
    private fun insertTask(task: Task) {
        repository.insertTask(task)
        queryTasks(_currentFilter.value!!)
    }

    // Borra la tarea
    fun deleteTask(task: Task) {
        repository.deleteTask(task.getId())
        _onShowTaskDeleted.value = Event(task)
        queryTasks(_currentFilter.value!!)
    }

    fun deleteTaskByPosition(position: Int) {
        if (tasks.value?.isNotEmpty() == true && tasks.value!!.size > position ) {
            deleteTask(tasks.value!![position])
        } else {
            _onShowMessage.value = Event(application.getString(R.string.tasks_error_delete))
        }

    }

    // Borra todas las tareas mostradas actualmente en el RecyclerView.
    // Si no se estaba mostrando ninguna tarea, se muestra un mensaje
    // informativo en un SnackBar de que no hay tareas que borrar.
    fun deleteTasks() {
        if (_tasks.value?.isNotEmpty() == true) {
            repository.deleteTasks(_tasks.value!!.map { task -> task.getId() })
            queryTasks(_currentFilter.value!!)
        } else {
            _onShowMessage.value = Event(application.getString(R.string.tasks_no_tasks_to_delete))
        }
    }

    // Marca como completadas todas las tareas mostradas actualmente en el RecyclerView,
    // incluso si ya estaban completadas.
    // Si no se estaba mostrando ninguna tarea, se muestra un mensaje
    // informativo en un SnackBar de que no hay tareas que marcar como completadas.
    fun markTasksAsCompleted() {
        if (_tasks.value?.isNotEmpty() == true) {
            repository.markTasksAsCompleted(_tasks.value!!.map {task -> task.getId()})
            queryTasks(_currentFilter.value!!)
        } else {
            _onShowMessage.value = Event(application.getString(R.string.tasks_no_tasks_to_mark_as_completed))
        }
    }

    // Marca como pendientes todas las tareas mostradas actualmente en el RecyclerView,
    // incluso si ya estaban pendientes.
    // Si no se estaba mostrando ninguna tarea, se muestra un mensaje
    // informativo en un SnackBar de que no hay tareas que marcar como pendientes.
    fun markTasksAsPending() {
        if (_tasks.value?.isNotEmpty() == true) {
            repository.markTasksAsPending(_tasks.value!!.map {task -> task.getId()})
            queryTasks(_currentFilter.value!!)
        } else {
            _onShowMessage.value = Event(application.getString(R.string.tasks_no_tasks_to_mark_as_pending))
        }
    }

    // Hace que se envíe un Intent con la lista de tareas mostradas actualmente
    // en el RecyclerView.
    // Si no se estaba mostrando ninguna tarea, se muestra un Snackbar indicando
    // que no hay tareas que compartir.
    fun shareTasks() {
        if (tasks.value?.isNotEmpty() == true) {
            var textTasks = ""
            tasks.value!!.forEach {
                    if (it.isCompleted()) {
                        textTasks += application.getString(R.string.tasks_mnuFilterCompleted)
                    } else {
                        textTasks += application.getString(R.string.tasks_mnuFilterPending)
                    }
                    textTasks+=String.format(": %s%n", it.getConcept())
            }
            val intent = Intent(Intent.ACTION_SEND).apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, textTasks)
                type = "text/plain"
            }
            _onStartActivity.value = Event(intent)
        } else {
            _onShowMessage.value = Event(application.getString(R.string.tasks_no_tasks_to_share))
        }

    }


    // Actualiza el estado de completitud de la tarea recibida, atendiendo al
    // valor de isCompleted. Si es true la tarea es marcada como completada y
    // en caso contrario es marcada como pendiente.
    fun updateTaskCompletedState(task: Task, isCompleted: Boolean) {
        if (isCompleted) {
            repository.markTaskAsCompleted(task.getId())
            repository.setCompletedAt(task.getId(), application.getString(R.string.tasks_item_completedAt, sdf.format(Date())))
        } else {
            repository.markTaskAsPending(task.getId())
        }
        queryTasks(_currentFilter.value!!)
    }



    // Pide las tareas al repositorio, atendiendo al filtro recibido
    private fun queryTasks(filter: TasksActivityFilter) {
        when (filter) {
            TasksActivityFilter.ALL -> _tasks.value = repository.queryAllTasks().sortedByDescending { task -> task.getCreatedAt() }
            TasksActivityFilter.PENDING -> _tasks.value = repository.queryPendingTasks().sortedByDescending { task -> task.getCreatedAt() }
            TasksActivityFilter.COMPLETED -> _tasks.value = repository.queryCompletedTasks().sortedByDescending { task -> task.getCreatedAt() }
        }
    }

    // Retorna si el concepto recibido es válido (no es una cadena vacía o en blanco)
    private fun isValidConcept(concept: String): Boolean {
        return concept.trim().isNotBlank()
    }

}

