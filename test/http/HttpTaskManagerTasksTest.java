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

class HttpTaskManagerTasksTest extends HttpTaskServerTestBase {

    @Test
    void testAddTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Test Description", 0, Status.NEW,
                LocalDateTime.now(), 60);
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неверный статус код");

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode(), "Неверный статус код для GET запроса");

        Task[] tasks = gson.fromJson(getResponse.body(), Task[].class);
        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(1, tasks.length, "Некорректное количество задач");
        assertEquals("Test Task", tasks[0].getTaskName(), "Некорректное имя задачи");
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Test Description", 0, Status.NEW,
                LocalDateTime.now(), 60);
        String taskJson = gson.toJson(task);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode(), "Неверный статус код при создании");

        Task createdTask = gson.fromJson(postResponse.body(), Task.class);
        int taskId = createdTask.getTaskId();

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/" + taskId))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, getResponse.statusCode(), "Неверный статус код");
        assertNotNull(getResponse.body(), "Тело ответа пустое");

        Task responseTask = gson.fromJson(getResponse.body(), Task.class);
        assertNotNull(responseTask, "Задача не вернулась");
        assertEquals(taskId, responseTask.getTaskId(), "Неверный ID задачи");
        assertEquals("Test Task", responseTask.getTaskName(), "Неверное имя задачи");
    }

    @Test
    void testGetTaskByIdNotFound() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Неверный статус код для несуществующей задачи");
    }

    @Test
    void testGetAllTasks() throws IOException, InterruptedException {
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

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Неверный статус код");
        assertNotNull(response.body(), "Тело ответа пустое");

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(tasks, "Задачи не вернулись");

        System.out.println("Number of tasks returned: " + tasks.length);
        for (Task task : tasks) {
            System.out.println("Task: " + task.getTaskName() + " (ID: " + task.getTaskId() + ")");
        }

        assertEquals(2, tasks.length, "Некорректное количество задач. Получено: " + tasks.length);

        boolean hasTask1 = false;
        boolean hasTask2 = false;
        for (Task task : tasks) {
            if ("Task 1".equals(task.getTaskName())) hasTask1 = true;
            if ("Task 2".equals(task.getTaskName())) hasTask2 = true;
        }
        assertTrue(hasTask1 && hasTask2, "Не все задачи вернулись");
    }

    @Test
    void testDeleteTask() throws IOException, InterruptedException {

        Task task = new Task("Test Task", "Test Description", 0, Status.NEW,
                LocalDateTime.now(), 60);
        String taskJson = gson.toJson(task);

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode(), "Неверный статус код при создании");

        Task createdTask = gson.fromJson(postResponse.body(), Task.class);
        int taskId = createdTask.getTaskId();

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/" + taskId))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode(), "Неверный статус код при удалении");

        HttpRequest getAfterRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/task/"))
                .GET()
                .build();
        HttpResponse<String> getAfterResponse = client.send(getAfterRequest, HttpResponse.BodyHandlers.ofString());
        Task[] tasksAfter = gson.fromJson(getAfterResponse.body(), Task[].class);
        assertEquals(0, tasksAfter.length, "Задача не была удалена");
    }
}