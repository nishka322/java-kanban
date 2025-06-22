package task;

import manager.Status;

import java.util.ArrayList;

public class Epic extends Task {

    private final ArrayList<Subtask> subtasks = new ArrayList<>();

    public Epic(String taskName, String taskDescription, int taskId, Status status) {
        super(taskName, taskDescription, taskId, status);
    }

    public Epic(String taskName, String taskDescription) {
        super(taskName, taskDescription);
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(ArrayList<Subtask> subtasks) {
        this.subtasks.clear();
        if (subtasks != null) {
            for (Subtask sub : subtasks) {
                if (sub.getTaskId() != this.getTaskId()) {
                    this.subtasks.add(sub);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "task.Epic{" +
                "taskName='" + getTaskName() + '\'' +
                ", taskDescription='" + getTaskDescription() + '\'' +
                ", taskId=" + getTaskId() +
                ", status=" + getStatus() +
                ", subtasks=" + subtasks +
                '}';
    }
}
