package task;

import manager.FileBackedTaskManager;
import manager.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final ArrayList<Subtask> subtasks = new ArrayList<>();
    private transient LocalDateTime endTime;

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

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void calculateTimes() {
        if (subtasks.isEmpty()) {
            setStartTime(null);
            setDuration(0);
            setEndTime(null);
            return;
        }

        List<Subtask> subtasksWithTime = subtasks.stream()
                .filter(subtask -> subtask.getStartTime() != null)
                .toList();

        if (subtasksWithTime.isEmpty()) {
            setStartTime(null);
            setDuration(0);
            setEndTime(null);
            return;
        }

        LocalDateTime earliestStart = subtasksWithTime.stream()
                .map(Subtask::getStartTime)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        LocalDateTime latestEnd = subtasksWithTime.stream()
                .map(Subtask::getEndTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        long totalDuration = subtasksWithTime.stream()
                .mapToLong(Subtask::getDuration)
                .sum();

        setStartTime(earliestStart);
        setDuration(totalDuration);
        setEndTime(latestEnd);
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
