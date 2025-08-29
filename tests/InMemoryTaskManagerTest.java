package manager;

import task.Epic;
import task.Subtask;
import task.Task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest extends manager.TaskManagerTest<InMemoryTaskManager> {

    @Override
    protected InMemoryTaskManager createTaskManager() {
        return new InMemoryTaskManager();
    }

    /* Специфичные тесты для InMemoryTaskManager */

    @Test
    @DisplayName("Проверка генерации уникальных ID")
    void testIdGeneration() {
        InMemoryTaskManager manager = createTaskManager();

        Task task1 = manager.addTask(new Task("Task 1", "Description"));
        Task task2 = manager.addTask(new Task("Task 2", "Description"));
        Task task3 = manager.addTask(new Task("Task 3", "Description"));

        assertTrue(task1.getTaskId() > 0);
        assertTrue(task2.getTaskId() > task1.getTaskId());
        assertTrue(task3.getTaskId() > task2.getTaskId());
    }

    @Test
    @DisplayName("Загрузка задач через load методы")
    void testLoadMethods() {
        InMemoryTaskManager manager = createTaskManager();

        Task task = new Task("Loaded Task", "Description", 100, Status.NEW);
        Epic epic = new Epic("Loaded Epic", "Description", 200, Status.NEW);
        Subtask subtask = new Subtask("Loaded Subtask", "Description", 300, Status.NEW, 200);

        manager.loadTask(task);
        manager.loadEpic(epic);
        manager.loadSubtask(subtask);

        assertEquals(task, manager.getTask(100));
        assertEquals(epic, manager.getEpic(200));
        assertEquals(subtask, manager.getSubtask(300));
    }

    @Test
    @DisplayName("Установка счетчика ID")
    void testSetIdCounter() {
        InMemoryTaskManager manager = createTaskManager();
        manager.setIdCounter(500);

        Task task = manager.addTask(new Task("Task", "Description"));
        assertEquals(500, task.getTaskId());

        Task nextTask = manager.addTask(new Task("Next Task", "Description"));
        assertEquals(501, nextTask.getTaskId());
    }

    /* Тесты временных интервалов и приоритизации */

    @Test
    @DisplayName("Добавление задачи с временем в prioritizedTasks")
    void testAddTaskWithTimeToPrioritized() {
        InMemoryTaskManager manager = createTaskManager();

        Task task = new Task("Task with time", "Description");
        task.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task.setDuration(60);

        manager.addTask(task);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(1, prioritized.size());
        assertEquals(task, prioritized.getFirst());
    }

    @Test
    @DisplayName("Задачи без времени не попадают в prioritizedTasks")
    void testTaskWithoutTimeNotInPrioritized() {
        InMemoryTaskManager manager = createTaskManager();

        Task task = new Task("Task without time", "Description");
        manager.addTask(task);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertTrue(prioritized.isEmpty());
    }

    @Test
    @DisplayName("Проверка пересечения временных интервалов - положительный случай")
    void testTimeOverlapDetection() {
        InMemoryTaskManager manager = createTaskManager();

        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setDuration(60);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 30));
        task2.setDuration(60);

        manager.addTask(task1);

        assertThrows(IllegalArgumentException.class, () -> manager.addTask(task2),
                "Должно быть выброшено исключение при пересечении временных интервалов");
    }

    @Test
    @DisplayName("Обновление задачи с проверкой пересечения временных интервалов")
    void testUpdateTaskWithTimeOverlapCheck() {
        InMemoryTaskManager manager = createTaskManager();

        Task task1 = new Task("Task 1", "Description");
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task1.setDuration(60);

        Task task2 = new Task("Task 2", "Description");
        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 12, 0));
        task2.setDuration(60);

        manager.addTask(task1);
        manager.addTask(task2);

        task2.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 30));

        assertThrows(IllegalArgumentException.class, () -> manager.updateTask(task2),
                "Должно быть выброшено исключение при обновлении с пересечением временных интервалов");
    }

    @Test
    @DisplayName("Задачи с null startTime не попадают в prioritized список")
    void testTasksWithNullStartTimeNotInPrioritized() {
        InMemoryTaskManager manager = createTaskManager();

        Task task1 = new Task("Task with time", "Description");
        task1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));

        Task task2 = new Task("Task without time", "Description");

        manager.addTask(task1);
        manager.addTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(1, prioritized.size());
        assertEquals(task1, prioritized.getFirst());
    }

    @Test
    @DisplayName("Обновление эпика с подзадачами сохраняет временные интервалы")
    void testUpdateEpicWithSubtasksTimePreservation() {
        InMemoryTaskManager manager = createTaskManager();

        Epic epic = manager.addEpic(new Epic("Epic", "Description"));

        Subtask subtask1 = new Subtask("Subtask 1", "Description", epic.getTaskId());
        subtask1.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        subtask1.setDuration(30);

        Subtask subtask2 = new Subtask("Subtask 2", "Description", epic.getTaskId());
        subtask2.setStartTime(LocalDateTime.of(2024, 1, 1, 11, 0));
        subtask2.setDuration(30);

        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        assertEquals(2, manager.getPrioritizedTasks().size());

        Epic updatedEpic = new Epic("Updated Epic", "Description", epic.getTaskId(), Status.NEW);
        updatedEpic.getSubtasks().add(subtask1);
        updatedEpic.getSubtasks().add(subtask2);

        manager.updateEpic(updatedEpic);

        assertEquals(2, manager.getPrioritizedTasks().size());
    }

    @Test
    @DisplayName("Очистка prioritizedTasks при clear методах")
    void testClearMethodsAffectPrioritizedTasks() {
        InMemoryTaskManager manager = createTaskManager();

        Task task = new Task("Task", "Description");
        task.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        manager.addTask(task);

        Epic epic = manager.addEpic(new Epic("Epic", "Description"));
        Subtask subtask = new Subtask("Subtask", "Description", epic.getTaskId());
        subtask.setStartTime(LocalDateTime.of(2024, 1, 1, 11, 0));
        manager.addSubtask(subtask);

        assertEquals(2, manager.getPrioritizedTasks().size());

        manager.clearTasks();
        assertEquals(1, manager.getPrioritizedTasks().size());

        manager.clearSubtasks();
        assertTrue(manager.getPrioritizedTasks().isEmpty());

        subtask = new Subtask("Subtask 2", "Description", epic.getTaskId());
        subtask.setStartTime(LocalDateTime.of(2024, 1, 1, 12, 0));
        manager.addSubtask(subtask);

        assertEquals(1, manager.getPrioritizedTasks().size());
        manager.clearEpics();
        assertTrue(manager.getPrioritizedTasks().isEmpty());
    }

    @Test
    @DisplayName("Задачи удаляются из истории при удалении")
    void testTaskRemovalFromHistory() {
        InMemoryTaskManager manager = createTaskManager();

        Task task = manager.addTask(new Task("Task", "Description"));
        manager.getTask(task.getTaskId());

        assertEquals(1, manager.getHistory().size());

        manager.deleteTask(task.getTaskId());
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    @DisplayName("Обновление задачи без изменения времени не вызывает исключений")
    void testUpdateTaskWithoutTimeChange() {
        InMemoryTaskManager manager = createTaskManager();

        Task task = new Task("Task", "Description");
        task.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        task.setDuration(60);

        manager.addTask(task);

        task.setTaskName("Updated Task");

        assertDoesNotThrow(() -> manager.updateTask(task));
        assertEquals("Updated Task", manager.getTask(task.getTaskId()).getTaskName());
    }

    @Test
    @DisplayName("Добавление задачи с null временем после задач с временем")
    void testAddTaskWithNullTimeAfterTimedTasks() {
        InMemoryTaskManager manager = createTaskManager();

        Task timedTask = new Task("Timed Task", "Description");
        timedTask.setStartTime(LocalDateTime.of(2024, 1, 1, 10, 0));
        manager.addTask(timedTask);

        Task nullTimeTask = new Task("Null Time Task", "Description");
        assertDoesNotThrow(() -> manager.addTask(nullTimeTask));

        assertEquals(1, manager.getPrioritizedTasks().size());
        assertEquals(2, manager.getTasks().size());
    }
}