package manager;

import java.io.File;

public class Managers {
    private static final File DEFAULT_FILE = new File("tasks.csv");

    public static TaskManager getDefault() {
        return new FileBackedTaskManager(DEFAULT_FILE);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}