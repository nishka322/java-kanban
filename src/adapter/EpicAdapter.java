package adapter;

import com.google.gson.*;
import manager.Status;
import task.Epic;

import java.lang.reflect.Type;

public class EpicAdapter implements JsonSerializer<Epic>, JsonDeserializer<Epic> {

    @Override
    public JsonElement serialize(Epic epic, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("taskName", epic.getTaskName());
        jsonObject.addProperty("taskDescription", epic.getTaskDescription());
        jsonObject.addProperty("taskId", epic.getTaskId());
        jsonObject.addProperty("status", epic.getStatus().name());
        return jsonObject;
    }

    @Override
    public Epic deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();

        String taskName = jsonObject.get("taskName").getAsString();
        String taskDescription = jsonObject.get("taskDescription").getAsString();
        int taskId = jsonObject.get("taskId").getAsInt();
        Status status = Status.valueOf(jsonObject.get("status").getAsString());

        return new Epic(taskName, taskDescription, taskId, status);
    }
}