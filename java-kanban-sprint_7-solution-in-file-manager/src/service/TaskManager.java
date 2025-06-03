package service;

import task.Epic;
import task.Subtask;
import task.Task;

import java.util.List;
import java.util.TreeSet;

public interface TaskManager {

    int generateId();

    int addTask(Task task);

    int addSubtask(Subtask subtask);

    int addEpic(Epic epic);

    Task getTask(int id);

    Subtask getSubtask(int id);

    Epic getEpic(int id);

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    void removeTask(int id);

    void removeAllTasks();

    void removeSubtask(int id);

    void removeEpic(int id);

    List<Subtask> getSubtasksByEpic(int epicId);

    void updateTask(Task task);

    public List<Task> getHistory();

    TreeSet<Task> getPrioritizedTasks();
}
