package manager;

import adapter.EpicAdapter;
import adapter.SubtaskAdapter;
import adapter.TaskAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import task.Epic;
import task.Subtask;
import task.Task;

import java.io.File;

public class Managers {
    private static final File DEFAULT_FILE = new File("tasks.csv");

    public static TaskManager getDefault() {
        return new FileBackedTaskManager(DEFAULT_FILE);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static Gson getGson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(Task.class, new TaskAdapter())
                .registerTypeAdapter(Epic.class, new EpicAdapter())
                .registerTypeAdapter(Subtask.class, new SubtaskAdapter())
                .create();
    }
}