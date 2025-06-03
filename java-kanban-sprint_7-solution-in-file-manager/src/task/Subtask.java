package task;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {

    private int epicId;

    public Subtask(String title, String description, int id, int epicId, TaskStatus status,TaskType type) {
        super(title, description, id, status,type);
        this.epicId = epicId;
    }

    @Override
    public LocalDateTime endTime(LocalDateTime startTime, Duration duration) {
        return super.endTime(startTime, duration);
    }

    public int getEpicId() {
        return epicId;
    }

}

