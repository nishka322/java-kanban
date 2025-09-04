package http;

import com.sun.net.httpserver.HttpServer;
import http.handlers.*;
import manager.Managers;
import manager.TaskManager;


import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private final HttpServer httpServer;
    private static final int PORT = 8080;

    public HttpTaskServer() throws IOException, InterruptedException {
        TaskManager taskManager = Managers.getDefault();
        this.httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);

        httpServer.createContext("/tasks/task/", new TaskHandler(taskManager));
        httpServer.createContext("/tasks/epic/", new EpicHandler(taskManager));
        httpServer.createContext("/tasks/subtask/", new SubtaskHandler(taskManager));
        httpServer.createContext("/tasks/subtask/epic/", new SubtaskByEpicHandler(taskManager));
        httpServer.createContext("/tasks/history/", new HistoryHandler(taskManager));
        httpServer.createContext("/tasks/", new TasksHandler(taskManager));
        httpServer.createContext("/tasks/prioritized", new PrioritizedHandler(taskManager));
    }

    public void start() {
        httpServer.start();
        System.out.println("HTTP Task Server started on port " + PORT);
    }

    public void stop() {
        httpServer.stop(1);
        System.out.println("HTTP Task Server stopped");
    }

    public static void main(String[] args) {
        try {
            HttpTaskServer server = new HttpTaskServer();
            server.start();

            Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
        } catch (IOException | InterruptedException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }
}