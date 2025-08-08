import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import manager.Status;
import org.junit.jupiter.api.Test;
import task.Task;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    @Test
    void addSingleTask() {
        HistoryManager manager = new InMemoryHistoryManager();
        Task task = new Task("Test", "Description", 1, Status.NEW);
        manager.add(task);
        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "История должна содержать 1 задачу");
        assertEquals(task, history.get(0), "Задачи должны совпадать");
    }

    @Test
    void addMultipleTasks() {
        HistoryManager manager = new InMemoryHistoryManager();
        Task task1 = new Task("Task1", "Desc1", 1, Status.NEW);
        Task task2 = new Task("Task2", "Desc2", 2, Status.IN_PROGRESS);
        manager.add(task1);
        manager.add(task2);
        List<Task> history = manager.getHistory();
        assertEquals(2, history.size(), "История должна содержать 2 задачи");
        assertEquals(task1, history.get(0), "Первая задача не совпадает");
        assertEquals(task2, history.get(1), "Вторая задача не совпадает");
    }

    @Test
    void duplicateTaskMovedToEnd() {
        HistoryManager manager = new InMemoryHistoryManager();
        Task task = new Task("Task", "Desc", 1, Status.NEW);
        manager.add(task);
        manager.add(new Task("Updated", "Desc", 1, Status.DONE));
        List<Task> history = manager.getHistory();
        assertEquals(1, history.size(), "История должна содержать 1 задачу после обновления");
        assertEquals("Updated", history.get(0).getTaskName(), "Имя задачи должно обновиться");
        assertEquals(Status.DONE, history.get(0).getStatus(), "Статус должен обновиться");
    }

    @Test
    void removeFromBeginning() {
        HistoryManager manager = createHistoryWithTasks(1, 2, 3);
        manager.remove(1);
        assertHistoryOrder(manager, List.of(2, 3));
    }

    @Test
    void removeFromMiddle() {
        HistoryManager manager = createHistoryWithTasks(1, 2, 3);
        manager.remove(2);
        assertHistoryOrder(manager, List.of(1, 3));
    }

    @Test
    void removeFromEnd() {
        HistoryManager manager = createHistoryWithTasks(1, 2, 3);
        manager.remove(3);
        assertHistoryOrder(manager, List.of(1, 2));
    }

    @Test
    void removeNonExistentTask() {
        HistoryManager manager = createHistoryWithTasks(1, 2);
        manager.remove(99);
        assertHistoryOrder(manager, List.of(1, 2));
    }

    @Test
    void taskCopyIsIndependent() {
        HistoryManager manager = new InMemoryHistoryManager();
        Task original = new Task("Original", "Desc", 1, Status.NEW);
        manager.add(original);

        original.setTaskName("Modified");
        original.setStatus(Status.DONE);

        Task fromHistory = manager.getHistory().get(0);
        assertEquals("Original", fromHistory.getTaskName(), "Имя не должно измениться");
        assertEquals(Status.NEW, fromHistory.getStatus(), "Статус не должен измениться");
    }

    @Test
    void emptyHistoryWhenNoTasks() {
        HistoryManager manager = new InMemoryHistoryManager();
        assertTrue(manager.getHistory().isEmpty(), "История должна быть пустой");
    }

    private HistoryManager createHistoryWithTasks(int... ids) {
        HistoryManager manager = new InMemoryHistoryManager();
        for (int id : ids) {
            manager.add(new Task("Task" + id, "Desc" + id, id, Status.NEW));
        }
        return manager;
    }

    private void assertHistoryOrder(HistoryManager manager, List<Integer> expectedIds) {
        List<Task> history = manager.getHistory();
        assertEquals(expectedIds.size(), history.size(), "Неверный размер истории");
        for (int i = 0; i < expectedIds.size(); i++) {
            assertEquals(expectedIds.get(i), history.get(i).getTaskId(),
                    "Неверный порядок задач в позиции " + i);
        }
    }
}