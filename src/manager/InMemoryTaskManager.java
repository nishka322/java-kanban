package manager;

import task.Epic;
import task.Subtask;
import task.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    private int idCounter = 1;

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public int generateId() {
        return idCounter++;
    }

    /* Add section */

    @Override
    public Task addTask(Task task) {
        if (task == null) {
            return null;
        }
        int newId = generateId();
        task.setTaskId(newId);
        tasks.put(newId, task);
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
        int newId = generateId();
        subtask.setTaskId(newId);
        subtasks.put(newId, subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.getSubtasks().add(subtask);
            updateEpicStatus(epic);
        }
        return subtask;
    }

    /* Update section */

    @Override
    public Task updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getTaskId())) {
            return null;
        }
        tasks.put(task.getTaskId(), task);
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        if (epic == null || !epics.containsKey(epic.getTaskId())) {
            return null;
        }
        int epicId = epic.getTaskId();
        Epic existing = epics.get(epicId);

        for (Subtask oldSub : new ArrayList<>(existing.getSubtasks())) {
            subtasks.remove(oldSub.getTaskId());
        }

        epics.put(epicId, epic);

        for (Subtask newSub : epic.getSubtasks()) {
            subtasks.put(newSub.getTaskId(), newSub);
        }

        updateEpicStatus(epic);
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        if (subtask == null || !subtasks.containsKey(subtask.getTaskId())) {
            return null;
        }
        int subtaskId = subtask.getTaskId();
        Subtask old = subtasks.put(subtaskId, subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.getSubtasks().remove(old);
            epic.getSubtasks().add(subtask);
            updateEpicStatus(epic);
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
        int countNew = 0;
        int countDone = 0;
        for (Subtask s : list) {
            switch (s.getStatus()) {
                case Status.NEW -> countNew++;
                case Status.DONE -> countDone++;
                default -> {
                }
            }
        }
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
        tasks.clear();
    }

    @Override
    public void clearEpics() {
        subtasks.clear();
        epics.clear();
    }

    @Override
    public void clearSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
            updateEpicStatus(epic);
        }
    }

    @Override
    public void deleteTask(int taskId) {
        tasks.remove(taskId);
        historyManager.remove(taskId);
    }

    @Override
    public void deleteEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (Subtask sub : epic.getSubtasks()) {
                subtasks.remove(sub.getTaskId());
                historyManager.remove(sub.getTaskId());
            }
        }
        historyManager.remove(epicId);
        epics.remove(epicId);
    }

    @Override
    public void deleteSubtask(int subtaskId) {
        Subtask sub = subtasks.remove(subtaskId);
        if (sub != null) {
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
}
