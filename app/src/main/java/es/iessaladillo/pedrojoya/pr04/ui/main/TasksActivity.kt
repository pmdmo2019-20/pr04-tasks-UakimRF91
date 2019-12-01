package es.iessaladillo.pedrojoya.pr04.ui.main

import android.content.ActivityNotFoundException
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isNotEmpty
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import es.iessaladillo.pedrojoya.pr04.R
import es.iessaladillo.pedrojoya.pr04.base.observeEvent
import es.iessaladillo.pedrojoya.pr04.data.LocalRepository
import es.iessaladillo.pedrojoya.pr04.data.entity.Task
import es.iessaladillo.pedrojoya.pr04.utils.hideKeyboard
import es.iessaladillo.pedrojoya.pr04.utils.invisibleUnless
import es.iessaladillo.pedrojoya.pr04.utils.setOnSwipeListener
import kotlinx.android.synthetic.main.tasks_activity.*


class TasksActivity : AppCompatActivity() {

    private var mnuFilter: MenuItem? = null

    private val viewModel: TasksActivityViewModel by viewModels {
        TasksActivityViewModelFactory(LocalRepository, application)
    }

    //Este es nuestro adaptador. Es un campo de la clase, no un método
    //also nos permite recibe como it el objeto de MainActivityAdapter y lo retorna después de ejecutar la lambda
    private val listAdapter: TasksActivityAdapter = TasksActivityAdapter().also {
        //Se le da funcionalidad a los tipos función del adaptador.
        //Si se llama a onCheckListener (se llamaba pulsando en el viewholder), para la posición en la que este, llama a checkMenuItem
        it.onCheckListener = { position -> setTaskChecked(position) }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tasks_activity)
        setupViews()
        //observeViewModelData es el canal de comunicación entre MainActivity y View Model.
        //El view Model no puede comunicarse con MainActivity directamente, entonces MainActivity tiene que observarlo
        //Lo que está observando son los LiveData que tiene el view model.
        observeViewModelData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity, menu)
        mnuFilter = menu.findItem(R.id.mnuFilter)
        return super.onCreateOptionsMenu(menu)
    }

    private fun observeViewModelData() {
        observeTasks()
        observeCurrentFilter()
        observeCurrentFilterMenuItemId()
        observeActivityTitle()
        observeLblEmptyViewText()
        observeOnStartActivity()
        observeOnShowMessage()
        observeOnShowTaskDeleted()
    }

    private fun observeTasks() {
        viewModel.tasks.observe(this) {
            showTasks(it)
        }
    }

    private fun observeCurrentFilter() {
        viewModel.currentFilter.observe(this) {

        }
    }

    private fun observeCurrentFilterMenuItemId() {
        viewModel.currentFilterMenuItemId.observe(this) {
            checkMenuItem(it)
        }
    }

    private fun observeActivityTitle() {
        viewModel.activityTitle.observe(this) {
            this.title = it
        }
    }

    private fun observeLblEmptyViewText() {
        viewModel.lblEmptyViewText.observe(this) {
            //TODO
        }
    }

    private fun observeOnStartActivity() {
        viewModel.onStartActivity.observeEvent(this) {
            if (lstTasks.isNotEmpty()) {
                startActivity(intent)
            }
        }
    }

    private fun observeOnShowMessage() {
        viewModel.onShowMessage.observeEvent(this) {
            Snackbar.make(lstTasks, it, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun observeOnShowTaskDeleted() {
        viewModel.onShowTaskDeleted.observeEvent(this) {
            val concept = it.getConcept()
            Snackbar.make( lstTasks, getString(R.string.tasks_task_deleted, concept), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.tasks_undo_deleting)) { viewModel.addTask(concept) }.show()
        }
    }

    private fun setupViews() {
        setupRecyclerView()
        imgAddTask.setOnClickListener {
            addTask()
        }
        txtConcept.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addTask()
                true
            } else {
                false
            }
        }
    }

    private fun addTask() {
        viewModel.addTask(txtConcept.text.toString())
        imgAddTask.hideKeyboard()
        txtConcept.setText("")
    }

    private fun setupRecyclerView() {
        lstTasks.run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
            itemAnimator = DefaultItemAnimator()
            adapter = listAdapter
            setOnSwipeListener { viewHolder, _ -> viewModel.deleteTaskByPosition(viewHolder.adapterPosition) }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mnuShare -> viewModel.shareTasks()
            R.id.mnuDelete -> viewModel.deleteTasks()
            R.id.mnuComplete -> viewModel.markTasksAsCompleted()
            R.id.mnuPending -> viewModel.markTasksAsPending()
            R.id.mnuFilterAll -> viewModel.filterAll()
            R.id.mnuFilterPending -> viewModel.filterPending()
            R.id.mnuFilterCompleted -> viewModel.filterCompleted()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun setTaskChecked(position: Int) {
        val task = listAdapter.currentList[position]
        viewModel.updateTaskCompletedState(task, !task.isCompleted())
        Toast.makeText(this, String.format("No sé por que no se reflejan los cambios. En teoría la lista del LiveData del ViewModel se ha refrescado al hacer sortedByDescending. El texto actual de la tarea es: %n%s%nEl estado de completitud de la tarea es: %s%nAl depurar he visto que ListAdapter no conserva los datos de la antigua lista (mList), sino que adopta los datos de la nueva lista y, a la hora de comparar, las dos listas tienen mismos datos (pese a ser distintas listas).%nAdemás ocurre que, al cambiar de filtro, los datos sí se reflejan correctamente así que las tareas están correctamente en la lista pese a no estar pintadas. No he conseguido encontrar en dónde estoy fallando.", task.getText(), task.isCompleted()), Toast.LENGTH_LONG).show()
    }

    private fun showTasks(tasks: List<Task>) {
        lstTasks.post {
            listAdapter.submitList(tasks)
            lblEmptyView.invisibleUnless(tasks.isEmpty())
        }
    }

    private fun checkMenuItem(@MenuRes menuItemId: Int) {
        lstTasks.post {
            val item = mnuFilter?.subMenu?.findItem(menuItemId)
            item?.let { menuItem ->
                menuItem.isChecked = true
            }
        }
    }

}