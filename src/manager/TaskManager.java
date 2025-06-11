package manager;

import java.util.ArrayList;
import java.util.HashMap;

import task.*;

public class TaskManager {

    private int idCounter = 1;

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    private int generateId() {
        return idCounter++;
    }

    /* Add section */

    public Task addTask(Task task) {
        if (task == null) {
            return null;
        }
        int newId = generateId();
        task.setTaskId(newId);
        tasks.put(newId, task);
        return task;
    }

    public Epic addEpic(Epic epic) {
        if (epic == null) {
            return null;
        }
        int newId = generateId();
        epic.setTaskId(newId);
        epics.put(newId, epic);
        return epic;
    }

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

    public Task updateTask(Task task) {
        if (task == null || !tasks.containsKey(task.getTaskId())) {
            return null;
        }
        tasks.put(task.getTaskId(), task);
        return task;
    }

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

    private void updateEpicStatus(Epic epic) {
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
                default -> {}
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

    public Task getTask(int taskId) {
        return tasks.get(taskId);
    }

    public Epic getEpic(int epicId) {
        return epics.get(epicId);
    }

    public Subtask getSubtask(int subtaskId) {
        return subtasks.get(subtaskId);
    }

    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public ArrayList<Subtask> getEpicSubtasks(Epic epic) {
        return new ArrayList<>(epic.getSubtasks());
    }

    /* Delete section */

    public void clearTasks() {
        tasks.clear();
    }

    public void clearEpics() {
        subtasks.clear();
        epics.clear();
    }

    public void clearSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtasks().clear();
            updateEpicStatus(epic);
        }
    }

    public void deleteTask(int taskId) {
        tasks.remove(taskId);
    }

    public void deleteEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (Subtask sub : epic.getSubtasks()) {
                subtasks.remove(sub.getTaskId());
            }
        }
        epics.remove(epicId);
    }

    public void deleteSubtask(int subtaskId) {
        Subtask sub = subtasks.remove(subtaskId);
        if (sub != null) {
            Epic epic = epics.get(sub.getEpicId());
            if (epic != null) {
                epic.getSubtasks().remove(sub);
                updateEpicStatus(epic);
            }
        }
    }
}
