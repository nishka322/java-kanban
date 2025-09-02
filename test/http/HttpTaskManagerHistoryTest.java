package http;

import manager.Status;
import org.junit.jupiter.api.Test;
import task.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerHistoryTest extends HttpTaskServerTestBase {

    @Test
    void testGetHistory() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Description 1", 0, Status.NEW,
                LocalDateTime.now(), 60);
        Task task2 = new Task("Task 2", "Description 2", 0, Status.IN_PROGRESS,
                LocalDateTime.now().plusHours(2), 30);

        HttpRequest request1 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task1)))
                .build();
        HttpResponse<String> response1 = client.send(request1, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response1.statusCode(), "Неверный статус код для первой задачи");

        HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task2)))
                .build();
        HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response2.statusCode(), "Неверный статус код для второй задачи");

        Thread.sleep(100);

        Task[] tasks = getAllTasks();
        int taskId1 = tasks[0].getTaskId();
        int taskId2 = tasks[1].getTaskId();

        getTaskById(taskId1);
        getTaskById(taskId2);

        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/history/"))
                .GET()
                .build();

        HttpResponse<String> historyResponse = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, historyResponse.statusCode(), "Неверный статус код для истории");

        Task[] history = gson.fromJson(historyResponse.body(), Task[].class);
        assertNotNull(history, "История не вернулась");
        assertTrue(history.length >= 2, "В истории должно быть 2 задачи");
    }

    @Test
    void testGetEmptyHistory() throws IOException, InterruptedException {
        HttpRequest historyRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/history/"))
                .GET()
                .build();

        HttpResponse<String> historyResponse = client.send(historyRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, historyResponse.statusCode(), "Неверный статус код для пустой истории");

        Task[] history = gson.fromJson(historyResponse.body(), Task[].class);
        assertNotNull(history, "История не вернулась");
        assertEquals(0, history.length, "История должна быть пустой");
    }

    private HttpResponse<String> createTask(String taskJson) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private Task[] getAllTasks() throws IOException, InterruptedException {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(response.body(), Task[].class);
    }

    private HttpResponse<String> getTaskById(int taskId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/" + taskId))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}