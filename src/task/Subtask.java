package task;

import manager.FileBackedTaskManager;
import manager.Status;

import java.time.LocalDateTime;

public class Subtask extends Task {

    private int epicId;

    public Subtask(String taskName, String taskDescription, int taskId, Status status, int epicId) {
        super(taskName, taskDescription, taskId, status);
        if (taskId != epicId) {
            this.epicId = epicId;
        }
    }

    public Subtask(String taskName, String taskDescription, int epicId) {
        super(taskName, taskDescription);
        this.epicId = epicId;
    }

    public Subtask(String taskName, String taskDescription, int taskId, Status status, int epicId, LocalDateTime startTime, long duration) {
        super(taskName, taskDescription, taskId, status, startTime, duration);
        if (taskId != epicId) {
            this.epicId = epicId;
        }
    }

    public int getEpicId() {
        return epicId;
    }

    public void loadToManager(FileBackedTaskManager manager) {
        manager.loadSubtask(this);
    }

    @Override
    public String toString() {
        return "task.Subtask{" +
                "taskName='" + getTaskName() + '\'' +
                ", taskDescription='" + getTaskDescription() + '\'' +
                ", taskId=" + getTaskId() +
                ", status=" + getStatus() +
                ", epicId=" + epicId +
                '}';
    }
}
