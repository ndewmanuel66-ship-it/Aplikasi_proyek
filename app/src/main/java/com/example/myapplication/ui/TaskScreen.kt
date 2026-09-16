package com.example.myapplication.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.Priority
import com.example.myapplication.data.TaskEntity
import com.example.myapplication.ui.components.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(viewModel: TaskViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val stats by viewModel.stats.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedParentId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Task Hierarchy") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                selectedParentId = null
                showAddDialog = true
            }, containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ProjectHeaderCard(stats = stats)
            
            SearchFilterSortBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                currentFilter = uiState.filterOption,
                onFilterChange = viewModel::onFilterOptionChange,
                currentSort = uiState.sortOption,
                onSortChange = viewModel::onSortOptionChange
            )

            if (uiState.rootTasks.isEmpty()) {
                EmptyStateUI(onCreateClick = {
                    selectedParentId = null
                    showAddDialog = true
                })
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.rootTasks, key = { it.id }) { task ->
                        TaskNodeItem(
                            task = task,
                            level = 0,
                            viewModel = viewModel,
                            onAddSubtask = { parentId ->
                                selectedParentId = parentId
                                showAddDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        EnhancedAddTaskDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { judul, priority, date, cat ->
                viewModel.addTask(judul, selectedParentId, priority, date, cat)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TaskNodeItem(
    task: TaskEntity,
    level: Int,
    viewModel: TaskViewModel,
    onAddSubtask: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val subtasks by viewModel.getSubtasks(task.id).collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (level * 16).dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = if (subtasks.isEmpty()) Color.LightGray else MaterialTheme.colorScheme.primary
                )
            }

            Checkbox(
                checked = task.statusSelesai,
                onCheckedChange = { viewModel.toggleTaskDone(task) }
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = task.judulTugas,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (task.statusSelesai) TextDecoration.LineThrough else null,
                        color = if (task.statusSelesai) MaterialTheme.colorScheme.outline else Color.Unspecified
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PriorityBadge(priority = task.priority)
                    DueDateBadge(dueDate = task.dueDate)
                    if (task.category != null) {
                        Text(
                            text = "#${task.category}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            IconButton(onClick = { onAddSubtask(task.id) }) {
                Icon(Icons.Default.AddCircleOutline, null, modifier = Modifier.size(20.dp))
            }

            IconButton(onClick = { viewModel.deleteTask(task) }) {
                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column {
                subtasks.forEach { subtask ->
                    TaskNodeItem(
                        task = subtask,
                        level = level + 1,
                        viewModel = viewModel,
                        onAddSubtask = onAddSubtask
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedAddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Priority, Long?, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    var category by remember { mutableStateOf("") }
    
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Task Details") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Priority", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Priority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p.name) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                ) {
                    val dateStr = if (datePickerState.selectedDateMillis != null) {
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(datePickerState.selectedDateMillis!!))
                    } else "Set Due Date"
                    Icon(Icons.Default.DateRange, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(dateStr)
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (title.isNotBlank()) {
                    onConfirm(title, priority, datePickerState.selectedDateMillis, category.ifBlank { null })
                }
            }) { Text("Save Task") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("OK") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
