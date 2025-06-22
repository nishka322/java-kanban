package task;

import manager.Status;

public class Task {

    private String taskName;
    private String taskDescription;
    private int taskId;
    private Status status;

    public Task(String taskName, String taskDescription, int taskId, Status status) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.taskId = taskId;
        this.status = status;
    }

    public Task(String taskName, String taskDescription) {
        this.taskName = taskName;
        this.taskDescription = taskDescription;
        this.status = Status.NEW;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Task task = (Task) obj;
        return taskId == task.taskId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(taskId);
    }

    @Override
    public String toString() {
        return "task.Task{" +
               "taskName='" + taskName + '\'' +
               ", taskDescription='" + taskDescription + '\'' +
               ", taskId=" + taskId +
               ", status=" + status +
               '}';
    }
}
