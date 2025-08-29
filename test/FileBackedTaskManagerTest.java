package manager;

import task.Epic;
import task.Subtask;
import task.Task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest extends manager.TaskManagerTest<FileBackedTaskManager> {

    @TempDir
    Path tempDir;
    private File tempFile;
    private FileBackedTaskManager fileManager;

    @Override
    protected FileBackedTaskManager createTaskManager() {
        try {
            tempFile = File.createTempFile("test", ".csv", tempDir.toFile());
            return new FileBackedTaskManager(tempFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp file", e);
        }
    }

    @BeforeEach
    public void setUp() throws IOException {
        tempFile = File.createTempFile("test", ".csv", tempDir.toFile());
        fileManager = new FileBackedTaskManager(tempFile);
        taskManager = fileManager;
    }

    @AfterEach
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    @DisplayName("Сохранение и загрузка пустого менеджера")
    public void testSaveAndLoadEmptyManager() {
        fileManager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loadedManager.getSubtasks().isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    @DisplayName("Сохранение и загрузка нескольких задач разных типов")
    public void testSaveAndLoadMultipleTasks() {
        Task task = new Task("Test Task", "Task description");
        Epic epic = new Epic("Test Epic", "Epic description");

        fileManager.addTask(task);
        fileManager.addEpic(epic);

        Subtask subtask = new Subtask("Test Subtask", "Subtask description", epic.getTaskId());

        fileManager.addSubtask(subtask);

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
    public void testSaveAndLoadAfterUpdate() {
        Task task = new Task("Original Task", "Original description");
        fileManager.addTask(task);

        Task updatedTask = new Task("Updated Task", "Updated description", task.getTaskId(), Status.IN_PROGRESS);
        fileManager.updateTask(updatedTask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        Task loadedTask = loadedManager.getTasks().getFirst();
        assertEquals("Updated Task", loadedTask.getTaskName());
        assertEquals("Updated description", loadedTask.getTaskDescription());
        assertEquals(Status.IN_PROGRESS, loadedTask.getStatus());
    }

    @Test
    @DisplayName("Сохранение и загрузка после удаления задач")
    public void testSaveAndLoadAfterDelete() {
        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");
        fileManager.addTask(task1);
        fileManager.addTask(task2);

        fileManager.deleteTask(task1.getTaskId());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loadedManager.getTasks().size());
        assertEquals("Task 2", loadedManager.getTasks().getFirst().getTaskName());
    }

    @Test
    @DisplayName("Сохранение и загрузка эпика с несколькими подзадачами")
    public void testSaveAndLoadEpicWithMultipleSubtasks() {
        Epic epic = new Epic("Test Epic", "Epic description");
        fileManager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", epic.getTaskId());
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", epic.getTaskId());
        Subtask subtask3 = new Subtask("Subtask 3", "Description 3", epic.getTaskId());

        fileManager.addSubtask(subtask1);
        fileManager.addSubtask(subtask2);
        fileManager.addSubtask(subtask3);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        Epic loadedEpic = loadedManager.getEpics().getFirst();
        assertEquals(3, loadedManager.getSubtasks().size());
        assertEquals(3, loadedEpic.getSubtasks().size(), "У эпика должно быть 3 подзадачи");
        assertEquals(Status.NEW, loadedEpic.getStatus());
    }

    @Test
    @DisplayName("Проверка формата CSV файла")
    public void testCsvFileFormat() throws IOException {
        Task task = new Task("Test Task", "Test Description");
        fileManager.addTask(task);

        String fileContent = Files.readString(tempFile.toPath());
        String[] lines = fileContent.split("\n");

        assertEquals("id,type,name,status,description,epic,startTime,duration", lines[0].trim());

        String[] taskData = lines[1].split(",");
        assertEquals(String.valueOf(task.getTaskId()), taskData[0]);
        assertEquals("TASK", taskData[1]);
        assertEquals("Test Task", taskData[2]);
        assertEquals("NEW", taskData[3]);
        assertEquals("Test Description", taskData[4]);
        assertEquals("", taskData[5]);
        assertEquals("null", taskData[6]);
        assertEquals("0", taskData[7]);
        assertEquals(8, taskData.length);
    }

    @Test
    @DisplayName("Совместимость с InMemoryTaskManager")
    public void testCompatibilityWithInMemoryManager() {
        InMemoryTaskManager memoryManager = new InMemoryTaskManager();

        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");

        memoryManager.addTask(task1);
        memoryManager.addTask(task2);

        fileManager.addTask(task1);
        fileManager.addTask(task2);

        assertEquals(memoryManager.getTasks().size(), fileManager.getTasks().size());
        assertEquals(memoryManager.getTask(task1.getTaskId()).getTaskName(),
                fileManager.getTask(task1.getTaskId()).getTaskName());
    }

    @Test
    @DisplayName("Восстановление счетчика идентификаторов")
    public void testIdCounterRestoration() {
        Task task1 = new Task("Task 1", "Description 1");
        Task task2 = new Task("Task 2", "Description 2");
        Task task3 = new Task("Task 3", "Description 3");

        fileManager.addTask(task1);
        fileManager.addTask(task2);
        fileManager.addTask(task3);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        Task newTask = new Task("New Task", "New Description");
        loadedManager.addTask(newTask);

        assertTrue(newTask.getTaskId() > task3.getTaskId(),
                "Новый ID должен быть больше предыдущих");
    }

    // Тесты на исключения
    @Test
    @DisplayName("Загрузка из несуществующего файла")
    public void testLoadFromNonExistentFile() {
        File nonExistentFile = new File("nonexistent.csv");

        assertThrows(RuntimeException.class, () -> {
            FileBackedTaskManager.loadFromFile(nonExistentFile);
        });
    }

    @Test
    @DisplayName("Загрузка из пустого файла")
    public void testLoadFromEmptyFile() throws IOException {
        File emptyFile = File.createTempFile("empty", ".csv", tempDir.toFile());

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(emptyFile);

        assertTrue(loadedManager.getTasks().isEmpty());
        assertTrue(loadedManager.getEpics().isEmpty());
        assertTrue(loadedManager.getSubtasks().isEmpty());

        emptyFile.delete();
    }
}