# Implementation Plan - Enhanced Task & Subtask Planner

This plan outlines the enhancements for the "Task & Subtask Planner" app, including advanced data modeling, a redesigned UI with Material 3 components, and robust filtering/sorting capabilities.

## User Review Required

> [!IMPORTANT]
> The database schema will change (adding priority, due date, category). I will enable `fallbackToDestructiveMigration()` in `AppDatabase.kt` to handle this easily for development, but note that this will wipe existing data.

## Proposed Changes

### Data Layer

#### [MODIFY] [TaskEntity.kt](file:///C:/Users/Andrew/AndroidStudioProjects/MyApplication2/app/src/main/java/com/example/myapplication/data/TaskEntity.kt)
* Add `Priority` enum (`HIGH`, `MEDIUM`, `LOW`).
* Add `priority`, `dueDate` (Long?), and `category` (String?) fields to `TaskEntity`.

#### [MODIFY] [TaskDao.kt](file:///C:/Users/Andrew/AndroidStudioProjects/MyApplication2/app/src/main/java/com/example/myapplication/data/TaskDao.kt)
* Add queries for filtering by priority and category.
* Add sorting by due date, priority, and creation order.

#### [MODIFY] [AppDatabase.kt](file:///C:/Users/Andrew/AndroidStudioProjects/MyApplication2/app/src/main/java/com/example/myapplication/data/AppDatabase.kt)
* Increment version to `3`.
* Ensure `fallbackToDestructiveMigration()` is enabled.

---

### ViewModel Layer

#### [MODIFY] [TaskViewModel.kt](file:///C:/Users/Andrew/AndroidStudioProjects/MyApplication2/app/src/main/java/com/example/myapplication/ui/TaskViewModel.kt)
* Add states for `searchQuery`, `selectedPriority`, `completionFilter`, and `sortOption`.
* Combine these states with `taskDao.getAllTasks()` to create a reactive `uiState`.
* Add `stats` flow for task counts.

---

### UI Layer

#### [NEW] [TaskComponents.kt](file:///C:/Users/Andrew/AndroidStudioProjects/MyApplication2/app/src/main/java/com/example/myapplication/ui/components/TaskComponents.kt)
* Create `ProjectHeaderCard` with gradient background and stats row.
* Create `PriorityBadge` and `DueDateBadge` components.
* Create `EmptyStateUI` with vector illustration.
* Create `SearchFilterSortBar`.

#### [MODIFY] [TaskScreen.kt](file:///C:/Users/Andrew/AndroidStudioProjects/MyApplication2/app/src/main/java/com/example/myapplication/ui/TaskScreen.kt)
* Integrate `SearchFilterSortBar` and `ProjectHeaderCard`.
* Update `TaskNodeItem` with priority/date badges and strike-through text.
* Expand `AddTaskDialog` with inputs for all new fields.

## Verification Plan

### Automated Tests
* Run `gradle build` to ensure Room schema and Compose UI compile.
* Use `analyze_file` to verify syntax and potential errors in new components.

### Manual Verification
* **Data Persistence**: Add a task with priority and due date, restart app, and verify it's still there.
* **UI Interactions**:
    * Search for a task by title.
    * Filter tasks by "High" priority.
    * Sort tasks by "Due Date".
    * Toggle a task as completed and watch the progress bar and stats update.
* **Empty State**: Clear all tasks and verify the empty state illustration appears.
