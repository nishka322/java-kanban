package manager;

import task.Task;
import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {

    private static final int MAX_HISTORY_STORAGE = 10;
    private final List<Task> historyList = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (historyList.size() == MAX_HISTORY_STORAGE) {
            historyList.removeFirst();
        }
        // Сохраняем состояние задачи в момент добавления
        Task taskCopy = new Task(
                task.getTaskName(),
                task.getTaskDescription(),
                task.getTaskId(),
                task.getStatus()
        );
        historyList.add(taskCopy);
    }

    @Override
    public List<Task> getHistory() {
        return historyList;
    }
}
