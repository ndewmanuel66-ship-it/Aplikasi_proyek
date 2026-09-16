# Implementation Plan - Task & Subtask Planner

A hierarchical task management application using Room for persistence and Jetpack Compose for the UI, following MVVM architecture.

## Proposed Changes

### Build Configuration

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Andrew/AndroidStudioProjects/MyApplication2/gradle/libs.versions.toml)
- Add versions and library definitions for Room, Lifecycle ViewModel Compose, and Material Icons Extended.
- Add `kotlin-kapt` plugin definition.

#### [MODIFY] [build.gradle.kts (app)](file:///C:/Users/Andrew/AndroidStudioProjects/MyApplication2/app/build.gradle.kts)
- Apply `kotlin-kapt` plugin.
- Add Room, ViewModel Compose, and Material Icons dependencies.

### Data Layer

#### [NEW] [TaskEntity.kt](file:///C:/Users/Andrew/AndroidStudioProjects/MyApplication2/app/src/main/java/com/example/myapplication/data/TaskEntity.kt)
- Define `TaskEntity` with `id`, `parentId` (ForeignKey to `id`), `judulTugas`, and `statusSelesai`.

#### [NEW] [TaskDao.kt](file:///C:/Users/Andrew/AndroidStudioProjects/MyApplication2/app/src/main/java/com/example/myapplication/data/TaskDao.kt)
- Define DAO methods: `insert`, `update`, `delete`, `getRootTasks`, `getSubtasks`.

#### [NEW] [AppDatabase.kt](file:///C:/Users/Andrew/AndroidStudioProjects/MyApplication2/app/src/main/java/com/example/myapplication/data/AppDatabase.kt)
- Setup Room database class.

### ViewModel Layer

#### [NEW] [TaskViewModel.kt](file:///C:/Users/Andrew/AndroidStudioProjects/MyApplication2/app/src/main/java/com/example/myapplication/ui/TaskViewModel.kt)
- Manage `rootTasks` StateFlow.
- Provide methods for CRUD and subtask fetching.

### UI Layer

#### [NEW] [TaskScreen.kt](file:///C:/Users/Andrew/AndroidStudioProjects/MyApplication2/app/src/main/java/com/example/myapplication/ui/TaskScreen.kt)
- `TaskScreen`: Main UI container.
- `TaskNodeItem`: Recursive component for tasks and subtasks.
- `AddTaskDialog`: Input dialog for new tasks.

### Integration

#### [MODIFY] [MainActivity.kt](file:///C:/Users/Andrew/AndroidStudioProjects/MyApplication2/app/src/main/java/com/example/myapplication/MainActivity.kt)
- Initialize Room database and ViewModel.
- Set `TaskScreen` as the content.

## Verification Plan

### Automated Tests
- Run `gradle_build` to ensure code compiles and Room annotations are processed correctly.

### Manual Verification
- Deploy to an emulator/device.
- Add a root task.
- Add a subtask to the root task.
- Toggle completion status.
- Expand/collapse subtasks.
- Delete a task (verify subtasks are also deleted due to CASCADE).
