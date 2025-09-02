package task;

import manager.FileBackedTaskManager;
import manager.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final ArrayList<Subtask> subtasks = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String taskName, String taskDescription, int taskId, Status status) {
        super(taskName, taskDescription, taskId, status);
    }

    public Epic(String taskName, String taskDescription, int taskId, Status status, LocalDateTime startTime, long duration) {
        super(taskName, taskDescription, taskId, status, startTime, duration);
    }

    public Epic(String taskName, String taskDescription) {
        super(taskName, taskDescription);
    }

    public void loadToManager(FileBackedTaskManager manager) {
        manager.loadEpic(this);
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime() {
        this.endTime = calculateEndTime();
    }

    public int calculateDuration() {
        if (subtasks.isEmpty()) {
            return 0;
        }
        return Math.toIntExact(subtasks.stream()
                .mapToLong(Subtask::getDuration)
                .sum());
    }

    public LocalDateTime calculateEndTime() {
        if (subtasks.isEmpty()) {
            return null;
        }

        List<Subtask> subtasksWithTime = subtasks.stream()
                .filter(subtask -> subtask.getStartTime() != null && subtask.getDuration() > 0)
                .toList();

        if (subtasksWithTime.isEmpty()) {
            return null;
        }

        return subtasksWithTime.stream()
                .map(subtask -> subtask.getStartTime().plusMinutes(subtask.getDuration()))
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    public ArrayList<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(ArrayList<Subtask> subtasks) {
        this.subtasks.clear();
        if (subtasks != null) {
            List<Subtask> filteredSubtasks = subtasks.stream()
                    .filter(sub -> sub.getTaskId() != this.getTaskId())
                    .toList();
            this.subtasks.addAll(filteredSubtasks);
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
