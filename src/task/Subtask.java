package task;

import manager.Status;

public class Subtask extends Task {

    private int epicId;

    public Subtask(String taskName, String taskDescription, int taskId, Status status, int epicId) {
        super(taskName, taskDescription, taskId, status);
        this.epicId = epicId;
    }

    public Subtask(String taskName, String taskDescription, int epicId) {
        super(taskName, taskDescription);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
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
