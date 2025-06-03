package service.impl;

import service.HistoryManager;
import task.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final List<Task> history;

    public InMemoryHistoryManager() {
        this.history = new ArrayList<>();
        ;
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }

    @Override
    public void addHistory(Task task) {
        history.add(task);
        if (history.size() > 10) {
            history.remove(0);
        }
    }
}
