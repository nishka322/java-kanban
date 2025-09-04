package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Task;
import http.HttpMethod;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            HttpMethod httpMethod;

            try {
                httpMethod = HttpMethod.valueOf(exchange.getRequestMethod());
            } catch (IllegalArgumentException e) {
                sendNotFound(exchange);
                return;
            }

            if (httpMethod == HttpMethod.GET) {
                if (Pattern.matches("^/tasks/prioritized$", path)) {
                    handleGetPrioritizedTasks(exchange);
                } else {
                    sendNotFound(exchange);
                }
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }

    private void handleGetPrioritizedTasks(HttpExchange exchange) throws IOException {
        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        sendSuccess(exchange, gson.toJson(prioritizedTasks));
    }
}