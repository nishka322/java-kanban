package adapter;

import com.google.gson.*;
import manager.Status;
import task.Task;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskAdapter implements JsonSerializer<Task>, JsonDeserializer<Task> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public JsonElement serialize(Task task, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("taskName", task.getTaskName());
        jsonObject.addProperty("taskDescription", task.getTaskDescription());
        jsonObject.addProperty("taskId", task.getTaskId());
        jsonObject.addProperty("status", task.getStatus().name());

        if (task.getStartTime() != null) {
            jsonObject.addProperty("startTime", task.getStartTime().format(formatter));
        }

        jsonObject.addProperty("duration", task.getDuration());

        return jsonObject;
    }

    @Override
    public Task deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        String taskName = jsonObject.get("taskName").getAsString();
        String taskDescription = jsonObject.get("taskDescription").getAsString();
        int taskId = jsonObject.get("taskId").getAsInt();
        Status status = Status.valueOf(jsonObject.get("status").getAsString());

        Task task;
        if (jsonObject.has("startTime") && jsonObject.has("duration")) {
            LocalDateTime startTime = LocalDateTime.parse(jsonObject.get("startTime").getAsString(), formatter);
            long duration = jsonObject.get("duration").getAsLong();
            task = new Task(taskName, taskDescription, taskId, status, startTime, duration);
        } else {
            task = new Task(taskName, taskDescription, taskId, status);
        }

        return task;
    }
}