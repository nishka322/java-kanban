package manager;

import task.Epic;
import task.Subtask;
import task.Task;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        Task task1 = new Task("Задача 1", "Описание задачи 1");
        taskManager.addTask(task1);

        Task task2 = new Task("Задача 2", "Описание задачи 2");
        taskManager.addTask(task2);

        Epic epicWithSubtasks = new Epic("Эпик с подзадачами", "Описание эпика с подзадачами");
        taskManager.addEpic(epicWithSubtasks);

        Subtask subtask1 = new Subtask("Подзадача 1", "Описание подзадачи 1", epicWithSubtasks.getTaskId());
        Subtask subtask2 = new Subtask("Подзадача 2", "Описание подзадачи 2", epicWithSubtasks.getTaskId());
        Subtask subtask3 = new Subtask("Подзадача 3", "Описание подзадачи 3", epicWithSubtasks.getTaskId());

        taskManager.addSubtask(subtask1);
        taskManager.addSubtask(subtask2);
        taskManager.addSubtask(subtask3);

        Epic epicWithoutSubtasks = new Epic("Эпик без подзадач", "Описание эпика без подзадач");
        taskManager.addEpic(epicWithoutSubtasks);

        System.out.println("Созданы задачи:");
        printAllTasks(taskManager);
        System.out.println();

        System.out.println("Запрашиваем задачу 1...");
        taskManager.getTask(task1.getTaskId());
        printHistory(taskManager);

        System.out.println("Запрашиваем задачу 2...");
        taskManager.getTask(task2.getTaskId());
        printHistory(taskManager);

        System.out.println("Запрашиваем эпик с подзадачами...");
        taskManager.getEpic(epicWithSubtasks.getTaskId());
        printHistory(taskManager);

        System.out.println("Запрашиваем подзадачу 1...");
        taskManager.getSubtask(subtask1.getTaskId());
        printHistory(taskManager);

        System.out.println("Повторно запрашиваем задачу 1...");
        taskManager.getTask(task1.getTaskId());
        printHistory(taskManager);

        System.out.println("Запрашиваем подзадачу 2...");
        taskManager.getSubtask(subtask2.getTaskId());
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