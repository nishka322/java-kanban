package http.handlers;

import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import task.Epic;
import http.HttpMethod;

import java.io.IOException;

public class EpicHandler extends BaseHttpHandler {

    public EpicHandler(TaskManager taskManager) {
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

            switch (httpMethod) {
                case GET:
                    handleGetRequest(exchange, path, "/tasks/epic/",
                            v -> taskManager.getEpics(),
                            taskManager::getEpic);
                    break;
                case POST:
                    handlePostRequest(exchange, Epic.class,
                            taskManager::addEpic,
                            taskManager::updateEpic,
                            epic -> epic.getTaskId() == 0);
                    break;
                case DELETE:
                    handleDeleteRequest(exchange, path, "/tasks/epic/",
                            taskManager::clearEpics,
                            taskManager::getEpic,
                            taskManager::deleteEpic);
                    break;
                default:
                    sendNotFound(exchange);
            }
        } catch (Exception e) {
            sendInternalServerError(exchange);
        }
    }
}