import java.util.ArrayList;

public class Epic extends Task {

    private ArrayList<Subtask> subtasks = new ArrayList<>();

    public Epic(String taskName, String taskDescription, int taskId, Status status) {
        super(taskName, taskDescription, taskId, status);
    }

    public Epic(String taskName, String taskDescription) {
        super(taskName, taskDescription);
    }

    public void addSubtask(Subtask subtask) {
        if (subtask != null) {
            subtasks.add(subtask);
        }
    }

    public void removeSubtask(Subtask subtask) {
        if (subtask != null) {
            subtasks.remove(subtask);
        }
    }

    public void clearSubtasks() {
        subtasks.clear();
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(ArrayList<Subtask> subtasks) {
        if (subtasks != null) {
            this.subtasks = subtasks;
        } else {
            this.subtasks.clear();
        }
    }

    @Override
    public String toString() {
        return "Epic{" +
                "taskName='" + getTaskName() + '\'' +
                ", taskDescription='" + getTaskDescription() + '\'' +
                ", taskId=" + getTaskId() +
                ", status=" + getStatus() +
                ", subtasks=" + subtasks +
                '}';
    }
}
