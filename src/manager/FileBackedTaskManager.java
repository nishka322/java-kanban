package manager;

import exception.ManagerSaveException;
import task.Epic;
import task.Subtask;
import task.Task;
import task.TypeTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        Task result = super.updateTask(task);
        if (result != null) {
            save();
        }
        return result;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic result = super.updateEpic(epic);
        if (result != null) {
            save();
        }
        return result;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask result = super.updateSubtask(subtask);
        if (result != null) {
            save();
        }
        return result;
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
            String header = "id,type,name,status,description,epic,startTime,duration\n";

            String content = Stream.of(getTasks(), getEpics(), getSubtasks())
                    .filter(list -> list != null && !list.isEmpty())
                    .flatMap(List::stream)
                    .filter(Objects::nonNull)
                    .map(this::toString)
                    .collect(Collectors.joining("\n"));

            Files.writeString(file.toPath(), header + content);

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения файла", e);
        }
    }

    private String toString(Task task) {
        String type = TypeTask.valueOf(task.getClass().getSimpleName().toUpperCase()).toString();
        String epicId = (task instanceof Subtask) ? String.valueOf(((Subtask) task).getEpicId()) : "";

        String startTimeStr = task.getStartTime() != null ? task.getStartTime().toString() : "null";
        String durationStr = String.valueOf(task.getDuration());

        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                task.getTaskId(), type, task.getTaskName(), task.getStatus(),
                task.getTaskDescription(), epicId, startTimeStr, durationStr);
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TypeTask type = TypeTask.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];
        int epicId = parts.length > 5 && !parts[5].isEmpty() ? Integer.parseInt(parts[5]) : -1;

        LocalDateTime startTime = parts.length > 6 && !"null".equals(parts[6]) ?
                LocalDateTime.parse(parts[6]) : null;
        long duration = parts.length > 7 ? Long.parseLong(parts[7]) : 0;

        switch (type) {
            case TASK:
                Task task = new Task(name, description, id, status, startTime, duration);
                task.setTaskId(id);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description, id, status, startTime, duration);
                epic.setTaskId(id);
                return epic;
            case SUBTASK:
                Subtask subtask = new Subtask(name, description, id, status, epicId, startTime, duration);
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

            int maxId = Arrays.stream(lines)
                    .skip(1)
                    .filter(line -> !line.isBlank())
                    .map(FileBackedTaskManager::fromString)
                    .peek(task -> task.loadToManager(manager))
                    .mapToInt(Task::getTaskId)
                    .max()
                    .orElse(0);

            manager.getSubtasks()
                    .forEach(subtask -> {
                        Epic epic = manager.getEpic(subtask.getEpicId());
                        if (epic != null) {
                            epic.getSubtasks().add(subtask);
                        }
                    });

            manager.getEpics()
                    .forEach(epic -> {
                        epic.calculateDuration();
                        manager.updateEpicStatus(epic);
                    });

            manager.setIdCounter(maxId + 1);

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка чтения файла", e);
        }

        return manager;
    }
}