package manager;

import exception.ManagerSaveException;
import task.Epic;
import task.Subtask;
import task.Task;
import task.TypeTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    @Override
    public Task addTask(Task task) {
        super.addTask(task);
        save();
        return task;
    }

    @Override
    public Epic addEpic(Epic epic) {
        super.addEpic(epic);
        save();
        return epic;
    }

    @Override
    public Subtask addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
        return subtask;
    }

    @Override
    public Task updateTask(Task task) {
        super.updateTask(task);
        save();
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
        return epic;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
        return subtask;
    }

    @Override
    public void deleteTask(int taskId) {
        super.deleteTask(taskId);
        save();
    }

    @Override
    public void deleteEpic(int epicId) {
        super.deleteEpic(epicId);
        save();
    }

    @Override
    public void deleteSubtask(int subtaskId) {
        super.deleteSubtask(subtaskId);
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubtasks() {
        super.clearSubtasks();
        save();
    }

    void save() {
        try {
            String header = "id,type,name,status,description,epic\n";
            StringBuilder content = new StringBuilder(header);

            for (Task task : getTasks()) {
                if (task != null) {
                    content.append(toString(task)).append("\n");
                }
            }

            for (Epic epic : getEpics()) {
                if (epic != null) {
                    content.append(toString(epic)).append("\n");
                }
            }

            for (Subtask subtask : getSubtasks()) {
                if (subtask != null) {
                    content.append(toString(subtask)).append("\n");
                }
            }

            Files.writeString(file.toPath(), content.toString());

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения файла", e);
        }
    }


    private String toString(Task task) {
        String type = TypeTask.valueOf(task.getClass().getSimpleName().toUpperCase()).toString();
        String epicId = (task instanceof Subtask) ? String.valueOf(((Subtask) task).getEpicId()) : "";
        return String.format("%d,%s,%s,%s,%s,%s",
                task.getTaskId(), type, task.getTaskName(), task.getStatus(), task.getTaskDescription(), epicId);
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TypeTask type = TypeTask.valueOf(parts[1]);
        String name = parts[2];
        String status = parts[3];
        String description = parts[4];
        int epicId = parts.length > 5 && !parts[5].isEmpty() ? Integer.parseInt(parts[5]) : -1;

        switch (type) {
            case TASK:
                Task task = new Task(name, description, id, Status.valueOf(status));
                task.setTaskId(id);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setTaskId(id);
                epic.setStatus(Status.valueOf(status));
                return epic;
            case SUBTASK:
                Subtask subtask = new Subtask(name, description, id, Status.valueOf(status), epicId);
                subtask.setTaskId(id);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи");
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\n");

            if (lines.length <= 1) {
                return manager;
            }

            int maxId = 0;

            for (int i = 1; i < lines.length; i++) {
                if (lines[i].isBlank()) continue;
                Task task = fromString(lines[i]);

                int id = task.getTaskId();

                task.loadToManager(manager);

                if (id > maxId) {
                    maxId = id;
                }
            }

            for (Subtask subtask : manager.getSubtasks()) {
                Epic epic = manager.getEpic(subtask.getEpicId());
                if (epic != null) {
                    epic.getSubtasks().add(subtask);
                }
            }

            for (Epic epic : manager.getEpics()) {
                manager.updateEpicStatus(epic);
            }

            manager.setIdCounter(maxId + 1);

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла", e);
        }

        return manager;
    }

    public static void main(String[] args) throws IOException {
        File file = File.createTempFile("tasks", ".csv");

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        Task task1 = new Task("Задача 1", "Описание Задачи 1");
        manager.addTask(task1);

        Epic epic1 = new Epic("Эпик 1", "Описание Эпика 1");
        manager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Подзадача 1", "Описание Подзадачи 1", epic1.getTaskId());
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание Подзадачи 2", epic1.getTaskId());
        manager.addSubtask(subtask2);

        System.out.println("Задачи в первом менеджере:");
        System.out.println("Задач: " + manager.getTasks().size());
        System.out.println("Эпиков: " + manager.getEpics().size());
        System.out.println("Подзадач: " + manager.getSubtasks().size());

        FileBackedTaskManager loadedManager = loadFromFile(file);

        List<Task> loadedTasks = loadedManager.getTasks();
        List<Epic> loadedEpics = loadedManager.getEpics();
        List<Subtask> loadedSubtasks = loadedManager.getSubtasks();

        System.out.println("\nЗадачи в загруженном менеджере:");
        System.out.println("Задач: " + loadedTasks.size());
        System.out.println("Эпиков: " + loadedEpics.size());
        System.out.println("Подзадач: " + loadedSubtasks.size());

        System.out.println("\nСравнение задач:");
        if (manager.getTasks().equals(loadedTasks) &&
                manager.getEpics().equals(loadedEpics) &&
                manager.getSubtasks().equals(loadedSubtasks)) {
            System.out.println("Все задачи, эпики и подзадачи были успешно загружены.");
        } else {
            System.out.println("Ошибка: задачи не совпадают.");
        }
    }
}