package service.impl;

import service.HistoryManager;
import service.Managers;
import service.TaskManager;
import task.Epic;
import task.Subtask;
import task.Task;

import java.util.*;


public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, Subtask> subtasks;
    private final HistoryManager historyManager;

    private int idCounter;

    public InMemoryTaskManager() {
        this.tasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.historyManager = Managers.getDefaultHistory();
        this.idCounter = 1;
    }

    @Override
    public int generateId() {
        return idCounter++;
    }

    @Override
    public int addTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            throw new IllegalArgumentException("Задача с полученным ID " + task.getId() + " уже есть.");
        }
        tasks.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public int addSubtask(Subtask subtask) {

        if (subtask.getEpicId() == subtask.getId()) {
            return -1;
        }

        subtasks.put(subtask.getId(), subtask);
        if (epics.containsKey(subtask.getEpicId())) {
            epics.get(subtask.getEpicId()).addSubtask(subtask);
        }
        return subtask.getId();

    }

    @Override
    public int addEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public Task getTask(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.addHistory(task);
        }
        return task;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.addHistory(subtask);
        }
        return subtask;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.addHistory(epic);
        }
        return epic;
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void removeTask(int id) {
        tasks.remove(id);
    }

    @Override
    public void removeAllTasks() {
        tasks.clear();
    }

    @Override
    public void removeSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null && epics.containsKey(subtask.getEpicId())) {
            epics.get(subtask.getEpicId()).removeSubtask(subtask);
        }
    }

    @Override
    public void removeEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Subtask subtask : epic.getSubtasks()) {
                subtasks.remove(subtask.getId());
            }
        }
    }

    @Override
    public List<Subtask> getSubtasksByEpic(int epicId) {
        if (epics.containsKey(epicId)) {
            return epics.get(epicId).getSubtasks();
        }
        return Collections.emptyList();
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public TreeSet<Task> getPrioritizedTasks() {
        List<Task> taskList = new ArrayList<>();
        taskList.addAll(tasks.values());
        taskList.addAll(epics.values());
        List<Task> list = taskList.stream().sorted(
                Comparator.comparing(Task::getStartTime)
                ).toList();
        return new TreeSet<>(list);
    }
}