package manager;

import task.Epic;
import task.Subtask;
import task.Task;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static manager.FileBackedTaskManager.loadFromFile;

public class Main {
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

        // Вторая часть кода
        TaskManager taskManager = Managers.getDefault();

        Task task1_2 = new Task("Задача 1", "Описание задачи 1");
        taskManager.addTask(task1_2);

        Task task2 = new Task("Задача 2", "Описание задачи 2");
        taskManager.addTask(task2);

        Epic epicWithSubtasks = new Epic("Эпик с подзадачами", "Описание эпика с подзадачами");
        taskManager.addEpic(epicWithSubtasks);

        Subtask subtask1_2 = new Subtask("Подзадача 1", "Описание подзадачи 1", epicWithSubtasks.getTaskId());
        Subtask subtask2_2 = new Subtask("Подзадача 2", "Описание подзадачи 2", epicWithSubtasks.getTaskId());
        Subtask subtask3 = new Subtask("Подзадача 3", "Описание подзадачи 3", epicWithSubtasks.getTaskId());

        taskManager.addSubtask(subtask1_2);
        taskManager.addSubtask(subtask2_2);
        taskManager.addSubtask(subtask3);

        Epic epicWithoutSubtasks = new Epic("Эпик без подзадач", "Описание эпика без подзадач");
        taskManager.addEpic(epicWithoutSubtasks);

        System.out.println("Созданы задачи:");
        printAllTasks(taskManager);
        System.out.println();

        System.out.println("Запрашиваем задачу 1...");
        taskManager.getTask(task1_2.getTaskId());
        printHistory(taskManager);

        System.out.println("Запрашиваем задачу 2...");
        taskManager.getTask(task2.getTaskId());
        printHistory(taskManager);

        System.out.println("Запрашиваем эпик с подзадачами...");
        taskManager.getEpic(epicWithSubtasks.getTaskId());
        printHistory(taskManager);

        System.out.println("Запрашиваем подзадачу 1...");
        taskManager.getSubtask(subtask1_2.getTaskId());
        printHistory(taskManager);

        System.out.println("Повторно запрашиваем задачу 1...");
        taskManager.getTask(task1_2.getTaskId());
        printHistory(taskManager);

        System.out.println("Запрашиваем подзадачу 2...");
        taskManager.getSubtask(subtask2_2.getTaskId());
        printHistory(taskManager);

        System.out.println("Запрашиваем эпик без подзадач...");
        taskManager.getEpic(epicWithoutSubtasks.getTaskId());
        printHistory(taskManager);

        System.out.println("Запрашиваем подзадачу 3...");
        taskManager.getSubtask(subtask3.getTaskId());
        printHistory(taskManager);

        System.out.println("Повторно запрашиваем задачу 2...");
        taskManager.getTask(task2.getTaskId());
        printHistory(taskManager);

        System.out.println("Удаляем задачу 2 (ID: " + task2.getTaskId() + ")...");
        taskManager.deleteTask(task2.getTaskId());
        System.out.println("История после удаления задачи 2:");
        printHistory(taskManager);

        System.out.println("Удаляем эпик с подзадачами (ID: " + epicWithSubtasks.getTaskId() + ")...");
        taskManager.deleteEpic(epicWithSubtasks.getTaskId());
        System.out.println("История после удаления эпика с подзадачами:");
        printHistory(taskManager);
    }

    private static void printHistory(TaskManager manager) {
        List<Task> history = manager.getHistory();
        System.out.println("Текущая история (" + history.size() + "):");
        for (Task task : history) {
            System.out.println("  " + taskToString(task));
        }
        System.out.println();
    }

    private static String taskToString(Task task) {
        if (task instanceof Epic) {
            return "Epic{id=" + task.getTaskId() + ", name='" + task.getTaskName() + "'}";
        } else if (task instanceof Subtask subtask) {
            return "Subtask{id=" + subtask.getTaskId() + ", name='" + subtask.getTaskName() + "', epicId=" + subtask.getEpicId() + "}";
        } else {
            return "Task{id=" + task.getTaskId() + ", name='" + task.getTaskName() + "'}";
        }
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTasks()) {
            System.out.println("  " + taskToString(task));
        }

        System.out.println("Эпики:");
        for (Epic epic : manager.getEpics()) {
            System.out.println("  " + taskToString(epic));
            for (Subtask subtask : manager.getEpicSubtasks(epic)) {
                System.out.println("    " + taskToString(subtask));
            }
        }
    }
}