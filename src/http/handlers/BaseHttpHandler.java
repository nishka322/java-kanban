package http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import manager.Managers;
import manager.TaskManager;


import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;
import java.util.regex.Pattern;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;
    protected final Gson gson;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = Managers.getGson();
    }

    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    protected void sendSuccess(HttpExchange exchange, String response) throws IOException {
        sendText(exchange, response, 200);
    }

    protected void sendCreated(HttpExchange exchange, String response) throws IOException {
        sendText(exchange, response, 201);
    }

    protected void sendNotFound(HttpExchange exchange) throws IOException {
        sendText(exchange, "Not Found", 404);
    }

    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendText(exchange, "Task overlaps with existing tasks", 406);
    }

    protected void sendInternalServerError(HttpExchange exchange) throws IOException {
        sendText(exchange, "Internal Server Error", 500);
    }

    protected void sendBadRequest(HttpExchange exchange) throws IOException {
        sendText(exchange, "Bad Request", 400);
    }

    protected String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    protected int parsePathId(String path) {
        try {
            return Integer.parseInt(path);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    protected <T> void handleGetRequest(HttpExchange exchange, String path, String basePath,
                                        Function<Void, java.util.List<T>> getAllFunction,
                                        Function<Integer, T> getByIdFunction) throws IOException {
        if (Pattern.matches("^" + basePath + "$", path)) {
            java.util.List<T> entities = getAllFunction.apply(null);
            sendSuccess(exchange, gson.toJson(entities));

        } else if (Pattern.matches("^" + basePath + "\\d+$", path)) {
            String[] pathParts = path.split("/");
            int id = parsePathId(pathParts[pathParts.length - 1]);

            if (id == -1) {
                sendBadRequest(exchange);
                return;
            }

            T entity = getByIdFunction.apply(id);
            if (entity == null) {
                sendNotFound(exchange);
            } else {
                sendSuccess(exchange, gson.toJson(entity));
            }
        } else {
            sendNotFound(exchange);
        }
    }

    protected <T> void handlePostRequest(HttpExchange exchange, Class<T> entityClass,
                                         Function<T, T> addFunction,
                                         Function<T, T> updateFunction,
                                         Function<T, Boolean> isNewFunction) throws IOException {
        try {
            String body = readRequestBody(exchange);
            T entity = gson.fromJson(body, entityClass);

            T result;
            if (isNewFunction.apply(entity)) {
                result = addFunction.apply(entity);
                sendCreated(exchange, gson.toJson(result));
            } else {
                result = updateFunction.apply(entity);
                sendSuccess(exchange, gson.toJson(result));
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange);
        } catch (IllegalStateException e) {
            sendHasInteractions(exchange);
        }
    }

    protected <T> void handleDeleteRequest(HttpExchange exchange, String path, String basePath,
                                           Runnable clearAllFunction,
                                           Function<Integer, T> getByIdFunction,
                                           java.util.function.Consumer<Integer> deleteByIdFunction) throws IOException {
        try {
            if (Pattern.matches("^" + basePath + "$", path)) {
                clearAllFunction.run();
                sendSuccess(exchange, "All entities cleared");

            } else if (Pattern.matches("^" + basePath + "\\d+$", path)) {
                String[] pathParts = path.split("/");
                int id = parsePathId(pathParts[pathParts.length - 1]);

                if (id == -1) {
                    sendBadRequest(exchange);
                    return;
                }

                T entityBefore = getByIdFunction.apply(id);
                if (entityBefore == null) {
                    sendNotFound(exchange);
                    return;
                }

                deleteByIdFunction.accept(id);

                T entityAfter = getByIdFunction.apply(id);
                if (entityAfter == null) {
                    sendSuccess(exchange, "Entity deleted");
                } else {
                    sendInternalServerError(exchange);
                }

            } else {
                sendNotFound(exchange);
            }
        } catch (Exception e) {
            System.err.println("Error in handleDeleteRequest: " + e.getMessage());
            sendInternalServerError(exchange);
        }
    }

    public abstract void handle(HttpExchange exchange) throws IOException;
}