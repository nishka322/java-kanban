package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Task;


import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!"GET".equals(exchange.getRequestMethod())) {
                sendNotFound(exchange);
                return;
            }

            String path = exchange.getRequestURI().getPath();

            if (Pattern.matches("^/tasks/history/$", path)) {
                handleGetHistory(exchange);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }

    private void handleGetHistory(HttpExchange exchange) throws IOException {
        List<Task> history = taskManager.getHistory();
        sendSuccess(exchange, gson.toJson(history));
    }
}