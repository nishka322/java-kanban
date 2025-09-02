package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Subtask;

import java.io.IOException;

public class SubtaskHandler extends BaseHttpHandler {

    public SubtaskHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            switch (method) {
                case "GET":
                    handleGetRequest(exchange, path, "/tasks/subtask/",
                            v -> taskManager.getSubtasks(),
                            taskManager::getSubtask);
                    break;
                case "POST":
                    handlePostRequest(exchange, Subtask.class,
                            taskManager::addSubtask,
                            taskManager::updateSubtask,
                            subtask -> subtask.getTaskId() == 0);
                    break;
                case "DELETE":
                    handleDeleteRequest(exchange, path, "/tasks/subtask/",
                            taskManager::clearSubtasks,
                            taskManager::getSubtask,
                            taskManager::deleteSubtask);
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }
}