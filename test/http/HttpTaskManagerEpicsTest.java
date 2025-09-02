package http;

import manager.Status;
import org.junit.jupiter.api.Test;
import task.Epic;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskManagerEpicsTest extends HttpTaskServerTestBase {

    @Test
    void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Test Description", 0, Status.NEW);
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), "Неверный статус код");

        Epic[] epics = getAllEpics();
        assertNotNull(epics, "Эпики не возвращаются");
        assertEquals(1, epics.length, "Некорректное количество эпиков");
        assertEquals("Test Epic", epics[0].getTaskName(), "Некорректное имя эпика");
    }

    @Test
    void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Test Description", 0, Status.NEW);
        String epicJson = gson.toJson(epic);

        HttpResponse<String> epicResponse = createEpic(epicJson);
        assertEquals(201, epicResponse.statusCode(), "Неверный статус код при создании эпика");

        Epic createdEpic = gson.fromJson(epicResponse.body(), Epic.class);
        int epicId = createdEpic.getTaskId();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/subtask/epic/" + epicId))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Неверный статус код для подзадач эпика");
    }

    @Test
    void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Test Description", 0, Status.NEW);
        String epicJson = gson.toJson(epic);

        HttpResponse<String> postResponse = createEpic(epicJson);
        assertEquals(201, postResponse.statusCode(), "Неверный статус код при создании");

        Epic createdEpic = gson.fromJson(postResponse.body(), Epic.class);
        int epicId = createdEpic.getTaskId();

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic/" + epicId))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode(), "Неверный статус код при удалении");

        Epic[] epicsAfter = getAllEpics();
        assertEquals(0, epicsAfter.length, "Эпик не был удален");
    }

    private HttpResponse<String> createEpic(String epicJson) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic/"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private Epic[] getAllEpics() throws IOException, InterruptedException {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/epic/"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        return gson.fromJson(response.body(), Epic[].class);
    }
}