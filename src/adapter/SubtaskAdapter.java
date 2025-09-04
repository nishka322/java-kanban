package adapter;

import com.google.gson.*;
import manager.Status;
import task.Subtask;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SubtaskAdapter implements JsonSerializer<Subtask>, JsonDeserializer<Subtask> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public JsonElement serialize(Subtask subtask, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("taskName", subtask.getTaskName());
        jsonObject.addProperty("taskDescription", subtask.getTaskDescription());
        jsonObject.addProperty("taskId", subtask.getTaskId());
        jsonObject.addProperty("status", subtask.getStatus().name());
        jsonObject.addProperty("epicId", subtask.getEpicId());
        jsonObject.addProperty("duration", subtask.getDuration());

        if (subtask.getStartTime() != null) {
            jsonObject.addProperty("startTime", subtask.getStartTime().format(formatter));
        }

        return jsonObject;
    }

    @Override
    public Subtask deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        String taskName = jsonObject.get("taskName").getAsString();
        String taskDescription = jsonObject.get("taskDescription").getAsString();
        int taskId = jsonObject.get("taskId").getAsInt();
        Status status = Status.valueOf(jsonObject.get("status").getAsString());
        int epicId = jsonObject.get("epicId").getAsInt();

        Subtask subtask;
        if (jsonObject.has("startTime") && jsonObject.has("duration")) {
            LocalDateTime startTime = LocalDateTime.parse(jsonObject.get("startTime").getAsString(), formatter);
            long duration = jsonObject.get("duration").getAsLong();
            subtask = new Subtask(taskName, taskDescription, taskId, status, epicId, startTime, duration);
        } else {
            subtask = new Subtask(taskName, taskDescription, taskId, status, epicId);
        }

        return subtask;
    }
}