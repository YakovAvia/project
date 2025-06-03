package task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yy");

    private List<Subtask> subtasks;

    private LocalDateTime endTime;

    public Epic(String title, String description, int id, TaskStatus status,TaskType type) {
        super(title, description, id, status,type);
        subtasks = new ArrayList<>();
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void addSubtask(Subtask subtask) {
        this.subtasks.add(subtask);
    }

    public void removeSubtask(Subtask subtask) {
        this.subtasks.remove(subtask);
    }

    @Override
    public LocalDateTime getStartTime() {
        return subtasks.stream().map(Subtask::getStartTime).min(LocalDateTime::compareTo).orElse(null);
    }

    @Override
    public Duration getDuration() {
        return subtasks.stream().map(Subtask::getDuration).reduce(Duration.ZERO, Duration::plus);
    }

    @Override
    public LocalDateTime endTime(LocalDateTime startTime, Duration duration) {
        return endTime = startTime.plus(duration);
    }

    @Override
    public void setStatus(TaskStatus status) {
        super.setStatus(status);
        for (Subtask subtask : subtasks) {
            subtask.setStatus(status);
        }
    }

    @Override
    public TaskStatus getStatus() {
        if (subtasks.isEmpty()) {
            return TaskStatus.NEW;
        }
        if (subtasks.stream().allMatch(subtask -> subtask.getStatus() == TaskStatus.NEW)) {
            return TaskStatus.NEW;
        } else if (subtasks.stream().allMatch(subtask -> subtask.getStatus() == TaskStatus.DONE)) {
            return TaskStatus.DONE;
        } else {
            return TaskStatus.IN_PROGRESS;
        }
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    public List<String> getEndTime() {
        return subtasks.stream().map(subtask -> LocalDateTime.from(subtask.endTime(subtask.getStartTime(),subtask.getDuration())).format(DATE_TIME_FORMATTER)).toList();
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
