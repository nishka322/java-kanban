package manager;

import task.Epic;
import task.Subtask;
import task.Task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("test", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    @DisplayName("Сохранение и загрузка пустого менеджера")
    void testSaveAndLoadEmptyManager() {
        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loadedManager.getSubtasks().isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    @DisplayName("Сохранение и загрузка нескольких задач разных типов")
    void testSaveAndLoadMultipleTasks() {
        Task task = new Task("Test Task", "Task description");
        Epic epic = new Epic("Test Epic", "Epic description");

        manager.addTask(task);
        manager.addEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Subtask description", epic.getTaskId());

        manager.addSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        List<Task> loadedTasks = loadedManager.getTasks();
        List<Epic> loadedEpics = loadedManager.getEpics();
        List<Subtask> loadedSubtasks = loadedManager.getSubtasks();

        assertEquals(1, loadedTasks.size(), "Должна быть 1 задача");
        assertEquals(1, loadedEpics.size(), "Должен быть 1 эпик");
        assertEquals(1, loadedSubtasks.size(), "Должна быть 1 подзадача");

        Task loadedTask = loadedTasks.getFirst();
        assertEquals(task.getTaskName(), loadedTask.getTaskName());
        assertEquals(task.getTaskDescription(), loadedTask.getTaskDescription());
        assertEquals(task.getStatus(), loadedTask.getStatus());

        Epic loadedEpic = loadedEpics.getFirst();
        assertEquals(epic.getTaskName(), loadedEpic.getTaskName());
        assertEquals(epic.getTaskDescription(), loadedEpic.getTaskDescription());

        Subtask loadedSubtask = loadedSubtasks.getFirst();
        assertEquals(subtask.getTaskName(), loadedSubtask.getTaskName());
        assertEquals(subtask.getTaskDescription(), loadedSubtask.getTaskDescription());
        assertEquals(subtask.getEpicId(), loadedSubtask.getEpicId());

        assertFalse(loadedEpic.getSubtasks().isEmpty(), "У эпика должны быть подзадачи");
        assertEquals(loadedSubtask.getTaskId(), loadedEpic.getSubtasks().getFirst().getTaskId());
    }

    @Test
    @DisplayName("Сохранение и загрузка после обновления задач")
    void testSaveAndLoadAfterUpdate() {
        Task task = new Task("Original Task", "Original description");
        manager.addTask(task);

        Task updatedTask = new Task("Updated Task", "Updated description", task.getTaskId(), Status.IN_PROGRESS);
        manager.updateTask(updatedTask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        Task loadedTask = loadedManager.getTasks().getFirst();
        assertEquals("Updated Task", loadedTask.getTaskName());
        assertEquals("Updated description", loadedTask.getTaskDescription());
        assertEquals(Status.IN_PROGRESS, loadedTask.getStatus());
    }

    @Test
    @DisplayName("Сохранение и загрузка после удаления задач")
    void testSaveAndLoadAfterDelete() {
        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");
        manager.addTask(task1);
        manager.addTask(task2);

        manager.deleteTask(task1.getTaskId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loadedManager.getTasks().size());
        assertEquals("Task 2", loadedManager.getTasks().getFirst().getTaskName());
    }

    @Test
    @DisplayName("Сохранение и загрузка эпика с несколькими подзадачами")
    void testSaveAndLoadEpicWithMultipleSubtasks() {
        Epic epic = new Epic("Test Epic", "Epic description");
        manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getTaskId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getTaskId());
        Subtask subtask3 = new Subtask("Subtask 3", "Description 3", epic.getTaskId());

        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        manager.addSubtask(subtask3);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        Epic loadedEpic = loadedManager.getEpics().getFirst();
        assertEquals(3, loadedManager.getSubtasks().size());
        assertEquals(3, loadedEpic.getSubtasks().size(), "У эпика должно быть 3 подзадачи");

        assertEquals(Status.NEW, loadedEpic.getStatus());
    }

    @Test
    @DisplayName("Проверка формата CSV файла")
    void testCsvFileFormat() throws IOException {
        Task task = new Task("Test Task", "Test Description");
        manager.addTask(task);

        String fileContent = Files.readString(tempFile.toPath());
        String[] lines = fileContent.split("\n");

        assertEquals("id,type,name,status,description,epic", lines[0].trim());

        String[] taskData = lines[1].split(",");
        assertEquals(String.valueOf(task.getTaskId()), taskData[0]);
        assertEquals("TASK", taskData[1]);
        assertEquals("Test Task", taskData[2]);
        assertEquals("NEW", taskData[3]);
        assertEquals("Test Description", taskData[4]);
        assertTrue(taskData.length == 5 || taskData[5].isEmpty());
    }

    @Test
    @DisplayName("Совместимость с InMemoryTaskManager")
    void testCompatibilityWithInMemoryManager() {
        InMemoryTaskManager memoryManager = new InMemoryTaskManager();

        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");

        memoryManager.addTask(task1);
        memoryManager.addTask(task2);

        manager.addTask(task1);
        manager.addTask(task2);

        assertEquals(memoryManager.getTasks().size(), manager.getTasks().size());
        assertEquals(memoryManager.getTask(task1.getTaskId()).getTaskName(),
                manager.getTask(task1.getTaskId()).getTaskName());
    }

    @Test
    @DisplayName("Восстановление счетчика идентификаторов")
    void testIdCounterRestoration() {
        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");
        Task task3 = new Task("Task 3", "Description 3");

        manager.addTask(task1);
        manager.addTask(task2);
        manager.addTask(task3);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        Task newTask = new Task("New Task", "New Description");
        loadedManager.addTask(newTask);

        assertTrue(newTask.getTaskId() > task3.getTaskId(),
                "Новый ID должен быть больше предыдущих");
    }

}

