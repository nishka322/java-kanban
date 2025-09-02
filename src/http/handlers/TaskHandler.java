package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Task;

import java.io.IOException;


public class TaskHandler extends BaseHttpHandler {

    public TaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            switch (method) {
                case "GET":
                    handleGetRequest(exchange, path, "/tasks/task/",
                            v -> taskManager.getTasks(),
                            taskManager::getTask);
                    break;
                case "POST":
                    handlePostRequest(exchange, Task.class,
                            taskManager::addTask,
                            taskManager::updateTask,
                            task -> task.getTaskId() == 0);
                    break;
                case "DELETE":
                    handleDeleteRequest(exchange, path, "/tasks/task/",
                            taskManager::clearTasks,
                            taskManager::getTask,
                            taskManager::deleteTask);
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }
}