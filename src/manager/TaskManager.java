package manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import task.*;

public interface TaskManager {

    int generateId();

    /* Add section */

    Task addTask(Task task);

    Epic addEpic(Epic epic);

    Subtask addSubtask(Subtask subtask);

    /* Update section */

    Task updateTask(Task task);

    Epic updateEpic(Epic epic);

    Subtask updateSubtask(Subtask subtask);

    void updateEpicStatus(Epic epic);

    /* Get section */

    Task getTask(int taskId);

    Epic getEpic(int epicId);

    Subtask getSubtask(int subtaskId);

    ArrayList<Task> getTasks();

    ArrayList<Epic> getEpics();

    ArrayList<Subtask> getSubtasks();

    ArrayList<Subtask> getEpicSubtasks(Epic epic);

    /* Delete section */

    void clearTasks();

    void clearEpics();

    void clearSubtasks();

    void deleteTask(int taskId);

    void deleteEpic(int epicId);

    void deleteSubtask(int subtaskId);

    /* History */

    List<Task> getHistory();
}
