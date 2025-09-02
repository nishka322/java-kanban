package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Epic;
import task.Subtask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class SubtaskByEpicHandler extends BaseHttpHandler {

    public SubtaskByEpicHandler(TaskManager taskManager) {
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

            if (Pattern.matches("^/tasks/subtask/epic/\\d+$", path)) {
                handleGetSubtasksByEpic(exchange, path);
            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }

    private void handleGetSubtasksByEpic(HttpExchange exchange, String path) throws IOException {
        String[] pathParts = path.split("/");
        int epicId = parsePathId(pathParts[4]);

        if (epicId == -1) {
            sendBadRequest(exchange);
            return;
        }

        Epic epic = taskManager.getEpic(epicId);
        if (epic == null) {
            sendNotFound(exchange);
        } else {
            ArrayList<Subtask> subtasks = taskManager.getEpicSubtasks(epic);
            sendSuccess(exchange, gson.toJson(subtasks));
        }
    }
}