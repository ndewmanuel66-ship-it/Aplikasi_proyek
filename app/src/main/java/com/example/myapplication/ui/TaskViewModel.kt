package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.Priority
import com.example.myapplication.data.TaskDao
import com.example.myapplication.data.TaskEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption {
    DUE_DATE, PRIORITY, CREATION
}

enum class FilterOption {
    ALL, PENDING, COMPLETED
}

data class TaskUiState(
    val rootTasks: List<TaskEntity> = emptyList(),
    val searchQuery: String = "",
    val selectedPriority: Priority? = null,
    val filterOption: FilterOption = FilterOption.ALL,
    val sortOption: SortOption = SortOption.CREATION
)

data class TaskStats(
    val total: Int = 0,
    val completed: Int = 0,
    val pending: Int = 0,
    val progress: Float = 0f
)

class TaskViewModel(private val taskDao: TaskDao) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedPriority = MutableStateFlow<Priority?>(null)
    val selectedPriority = _selectedPriority.asStateFlow()

    private val _filterOption = MutableStateFlow(FilterOption.ALL)
    val filterOption = _filterOption.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.CREATION)
    val sortOption = _sortOption.asStateFlow()

    val uiState: StateFlow<TaskUiState> = combine(
        taskDao.getAllTasks(),
        _searchQuery,
        _selectedPriority,
        _filterOption,
        _sortOption
    ) { tasks, query, priority, filter, sort ->
        val filteredTasks = tasks.filter { task ->
            val matchesQuery = task.judulTugas.contains(query, ignoreCase = true)
            val matchesPriority = priority == null || task.priority == priority
            val matchesFilter = when (filter) {
                FilterOption.ALL -> true
                FilterOption.PENDING -> !task.statusSelesai
                FilterOption.COMPLETED -> task.statusSelesai
            }
            matchesQuery && matchesPriority && matchesFilter
        }

        val sortedTasks = when (sort) {
            SortOption.DUE_DATE -> filteredTasks.sortedBy { it.dueDate ?: Long.MAX_VALUE }
            SortOption.PRIORITY -> filteredTasks.sortedByDescending { it.priority.ordinal }
            SortOption.CREATION -> filteredTasks.sortedByDescending { it.createdAt }
        }

        // We only want root tasks for the recursive UI
        val rootTasks = sortedTasks.filter { it.parentId == null }
        
        TaskUiState(
            rootTasks = rootTasks,
            searchQuery = query,
            selectedPriority = priority,
            filterOption = filter,
            sortOption = sort
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskUiState())

    val stats: StateFlow<TaskStats> = taskDao.getAllTasks().map { tasks ->
        val total = tasks.size
        val completed = tasks.count { it.statusSelesai }
        val pending = total - completed
        val progress = if (total == 0) 0f else completed.toFloat() / total
        TaskStats(total, completed, pending, progress)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskStats())

    fun getSubtasks(parentId: Long) = taskDao.getSubtasks(parentId)

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onPriorityFilterChange(priority: Priority?) {
        _selectedPriority.value = priority
    }

    fun onFilterOptionChange(option: FilterOption) {
        _filterOption.value = option
    }

    fun onSortOptionChange(option: SortOption) {
        _sortOption.value = option
    }

    fun addTask(
        judul: String,
        parentId: Long?,
        priority: Priority = Priority.MEDIUM,
        dueDate: Long? = null,
        category: String? = null
    ) {
        viewModelScope.launch {
            taskDao.insertTask(
                TaskEntity(
                    judulTugas = judul,
                    parentId = parentId,
                    priority = priority,
                    dueDate = dueDate,
                    category = category,
                    statusSelesai = false
                )
            )
        }
    }

    fun toggleTaskDone(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.updateTask(task.copy(statusSelesai = !task.statusSelesai))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            taskDao.deleteTask(task)
        }
    }
}
