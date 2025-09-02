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

class HttpTaskManagerPriorityTest extends HttpTaskServerTestBase {

    @Test
    void testGetPrioritizedTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task Early", "Early task", 0, Status.NEW,
                LocalDateTime.now().plusHours(1), 60);
        Task task2 = new Task("Task Late", "Late task", 0, Status.IN_PROGRESS,
                LocalDateTime.now().plusHours(3), 30);

        createTask(gson.toJson(task1));
        createTask(gson.toJson(task2));

        HttpRequest priorityRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/"))
                .GET()
                .build();

        HttpResponse<String> priorityResponse = client.send(priorityRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, priorityResponse.statusCode(), "Неверный статус код для приоритетных задач");

        Task[] prioritizedTasks = gson.fromJson(priorityResponse.body(), Task[].class);
        assertNotNull(prioritizedTasks, "Приоритетные задачи не вернулись");
        assertEquals(2, prioritizedTasks.length, "Должно быть 2 приоритетные задачи");

        assertEquals("Task Early", prioritizedTasks[0].getTaskName(),
                "Задачи должны быть отсортированы по времени начала");
    }

    @Test
    void testGetEmptyPrioritizedTasks() throws IOException, InterruptedException {
        HttpRequest priorityRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/"))
                .GET()
                .build();

        HttpResponse<String> priorityResponse = client.send(priorityRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, priorityResponse.statusCode(), "Неверный статус код для пустого списка");

        Task[] prioritizedTasks = gson.fromJson(priorityResponse.body(), Task[].class);
        assertNotNull(prioritizedTasks, "Приоритетные задачи не вернулись");
        assertEquals(0, prioritizedTasks.length, "Список приоритетных задач должен быть пустым");
    }

    private HttpResponse<String> createTask(String taskJson) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}