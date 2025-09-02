package manager;

import task.Epic;
import task.Subtask;
import task.Task;

import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    private int idCounter = 1;

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(Task::getStartTime,
                    Comparator.nullsLast(Comparator.naturalOrder()))
    );

    @Override
    public int generateId() {
        return idCounter++;
    }

    public void loadTask(Task task) {
        tasks.put(task.getTaskId(), task);
    }

    public void loadEpic(Epic epic) {
        epics.put(epic.getTaskId(), epic);
    }

    public void loadSubtask(Subtask subtask) {
        subtasks.put(subtask.getTaskId(), subtask);
    }

    public void setIdCounter(int id) {
        this.idCounter = id;
    }

    private boolean isTasksOverlap(Task task1, Task task2) {
        if (task1 == task2) {
            return false;
        }

        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();
        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            return false;
        }

        return !start1.isAfter(end2) && !end1.isBefore(start2);
    }

    private boolean isTaskOverlappingWithAny(Task taskToCheck) {
        if (taskToCheck.getStartTime() == null || taskToCheck.getEndTime() == null) {
            return false;
        }

        List<Task> tasksWithTime = prioritizedTasks.stream()
                .filter(task -> task.getStartTime() != null && task.getEndTime() != null)
                .toList();

        return tasksWithTime.stream()
                .anyMatch(existingTask -> isTasksOverlap(taskToCheck, existingTask));
    }

    private boolean isTaskOverlappingWithAnyExcept(Task taskToCheck, Task taskToExclude) {
        if (taskToCheck.getStartTime() == null || taskToCheck.getEndTime() == null) {
            return false;
        }

        List<Task> tasksWithTime = prioritizedTasks.stream()
                .filter(task -> task.getStartTime() != null && task.getEndTime() != null)
                .filter(task -> !task.equals(taskToExclude))
                .toList();

        return tasksWithTime.stream()
                .anyMatch(existingTask -> isTasksOverlap(taskToCheck, existingTask));
    }

    /* Add section */

    @Override
    public Task addTask(Task task) {
        if (task == null) {
            return null;
        }

        if (isTaskOverlappingWithAny(task)) {
            throw new IllegalArgumentException("Задача пересекается по времени с существующей задачей");
        }

        int newId = generateId();
        task.setTaskId(newId);
        tasks.put(newId, task);

        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return task;
    }

    @Override
    public Epic addEpic(Epic epic) {
        if (epic == null) {
            return null;
        }
        int newId = generateId();
        epic.setTaskId(newId);
        epics.put(newId, epic);
        return epic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        if (subtask == null) {
            return null;
        }

        if (isTaskOverlappingWithAny(subtask)) {
            throw new IllegalArgumentException("Подзадача пересекается по времени с существующей задачей");
        }

        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Эпик с ID " + subtask.getEpicId() + " не существует");
        }

        int newId = generateId();
        subtask.setTaskId(newId);
        subtasks.put(newId, subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.getSubtasks().add(subtask);
            updateEpicStatus(epic);
        }

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        return subtask;
    }

    /* Update section */

    @Override
    public Task updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getTaskId())) {
            return null;
        }

        Task oldTask = tasks.get(task.getTaskId());

        if (isTaskOverlappingWithAnyExcept(task, oldTask)) {
            throw new IllegalArgumentException("Обновленная задача пересекается по времени с другой задачей");
        }

        prioritizedTasks.remove(oldTask);
        tasks.put(task.getTaskId(), task);

        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getTaskId())) {
            return null;
        }

        int epicId = epic.getTaskId();
        Epic existing = epics.get(epicId);

        existing.getSubtasks().forEach(oldSub -> {
            prioritizedTasks.remove(oldSub);
            subtasks.remove(oldSub.getTaskId());
        });

        epics.put(epicId, epic);

        epic.getSubtasks().forEach(newSub -> {
            subtasks.put(newSub.getTaskId(), newSub);
            Optional.ofNullable(newSub.getStartTime())
                    .ifPresent(startTime -> prioritizedTasks.add(newSub));
        });

        updateEpicStatus(epic);
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getTaskId())) {
            return null;
        }

        if (!epics.containsKey(subtask.getEpicId())) {
            throw new IllegalArgumentException("Эпик с ID " + subtask.getEpicId() + " не существует");
        }

        int subtaskId = subtask.getTaskId();
        Subtask old = subtasks.get(subtaskId);

        prioritizedTasks.remove(old);

        subtasks.put(subtaskId, subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.getSubtasks().remove(old);
            epic.getSubtasks().add(subtask);
            updateEpicStatus(epic);
        }

        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
        return subtask;
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        ArrayList<Subtask> list = epic.getSubtasks();
        if (list.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        long countNew = list.stream()
                .filter(subtask -> subtask.getStatus().equals(Status.NEW))
                .count();

        long countDone = list.stream()
                .filter(subtask -> subtask.getStatus().equals(Status.DONE))
                .count();

        if (countNew == list.size()) {
            epic.setStatus(Status.NEW);
        } else if (countDone == list.size()) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    /* Get section */

    @Override
    public Task getTask(int taskId) {
        Task task = tasks.get(taskId);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtask(int subtaskId) {
        Subtask subtask = subtasks.get(subtaskId);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(Epic epic) {
        return new ArrayList<>(epic.getSubtasks());
    }

    /* Delete section */

    @Override
    public void clearTasks() {
        tasks.values()
                .forEach(task -> {
                    prioritizedTasks.remove(task);
                    historyManager.remove(task.getTaskId());
                });
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        subtasks.values()
                .forEach(sub -> {
                    prioritizedTasks.remove(sub);
                    historyManager.remove(sub.getTaskId());
                });
        subtasks.clear();

        epics.values()
                .forEach(epic -> historyManager.remove(epic.getTaskId()));
        epics.clear();
    }

    @Override
    public void clearSubtasks() {
        subtasks.values()
                .forEach(sub -> {
                    prioritizedTasks.remove(sub);
                    historyManager.remove(sub.getTaskId());
                });
        subtasks.clear();

        epics.values()
                .forEach(epic -> {
                    epic.getSubtasks().clear();
                    updateEpicStatus(epic);
                });
    }

    @Override
    public void deleteTask(int taskId) {
        tasks.remove(taskId);
        prioritizedTasks.remove(tasks.get(taskId));
        historyManager.remove(taskId);
    }

    @Override
    public void deleteEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            epic.getSubtasks()
                    .forEach(sub -> {
                        prioritizedTasks.remove(sub);
                        subtasks.remove(sub.getTaskId());
                        historyManager.remove(sub.getTaskId());
                    });
            historyManager.remove(epicId);
            epics.remove(epicId);
        }
    }

    @Override
    public void deleteSubtask(int subtaskId) {
        Subtask sub = subtasks.remove(subtaskId);
        if (sub != null) {
            prioritizedTasks.remove(sub);
            Epic epic = epics.get(sub.getEpicId());
            if (epic != null) {
                epic.getSubtasks().remove(sub);
                updateEpicStatus(epic);
            }
        }
        historyManager.remove(subtaskId);
    }

    /* History */

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }
}
