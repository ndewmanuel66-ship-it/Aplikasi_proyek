package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE parentId IS NULL")
    fun getRootTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE parentId = :parentId")
    fun getSubtasks(parentId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getTasksSortedByDate(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY priority DESC")
    fun getTasksSortedByPriority(): Flow<List<TaskEntity>>
}
