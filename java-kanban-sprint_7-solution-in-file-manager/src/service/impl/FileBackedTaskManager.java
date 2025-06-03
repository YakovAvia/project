package service.impl;

import service.Managers;
import service.TaskManager;
import task.*;

import java.io.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileBackedTaskManager extends InMemoryTaskManager {

    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd.MM.yy");
    static final TaskManager taskManager = Managers.getDefault();


    public static void main(String[] args) {
        File file = new File("tasks.csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        Task task = new Task("Task","Discription", taskManager.generateId(), TaskStatus.NEW, TaskType.TASK);
        Task task2 = new Task("Task2","Discription2", taskManager.generateId(), TaskStatus.NEW, TaskType.TASK);
        task.setStartTime(LocalDateTime.now().plusHours(2));
        task.setDuration(Duration.ofMinutes(200));
        System.out.println("Время завершения задачи: " + LocalDateTime.from(task.endTime(task.getStartTime(),task.getDuration())).format(DATE_TIME_FORMATTER));
        manager.addTask(task);
        Epic epic = new Epic("Epic", "Epic", taskManager.generateId(), TaskStatus.NEW, TaskType.EPIC);
        Epic epic2 = new Epic("Epi2c", "Epic2", taskManager.generateId(), TaskStatus.NEW, TaskType.EPIC);
        manager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask", "Description", taskManager.generateId(), epic.getId(), TaskStatus.NEW, TaskType.SUBTASK);
        Subtask subtask2 = new Subtask("Subtask2", "Description2", taskManager.generateId(), epic.getId(), TaskStatus.NEW, TaskType.SUBTASK);
        Subtask subtask3 = new Subtask("Subtask3", "Description3", taskManager.generateId(), epic.getId(), TaskStatus.NEW, TaskType.SUBTASK);
        Subtask subtask4 = new Subtask("Subtask3", "Description3", taskManager.generateId(), epic2.getId(), TaskStatus.NEW, TaskType.SUBTASK);
        subtask.setStartTime(LocalDateTime.now());
        subtask2.setStartTime(LocalDateTime.now().plusDays(3));
        subtask3.setStartTime(LocalDateTime.now().plusDays(6));
        subtask4.setStartTime(LocalDateTime.now().plusDays(10));
        subtask.setDuration(Duration.ofMinutes(100));
        subtask2.setDuration(Duration.ofMinutes(205));
        subtask3.setDuration(Duration.ofMinutes(300));
        subtask4.setDuration(Duration.ofMinutes(400));
        System.out.println( "\nВремя завершения 1 подзадачи: " + LocalDateTime.from(subtask.endTime(subtask.getStartTime(),subtask.getDuration())).format(DATE_TIME_FORMATTER) +
                "\nВремя завершения 2 подзадачи: " + LocalDateTime.from(subtask2.endTime(subtask2.getStartTime(),subtask2.getDuration())).format(DATE_TIME_FORMATTER) +
                "\nВремя завершения 3 подзадачи: " + LocalDateTime.from(subtask3.endTime(subtask3.getStartTime(),subtask3.getDuration())).format(DATE_TIME_FORMATTER));
        manager.addSubtask(subtask);
        manager.addSubtask(subtask2);
        manager.addSubtask(subtask3);
        epic2.addSubtask(subtask4);

        System.out.println( "\nСамая первая подзадача по дате начале в Epic: " + LocalDateTime.from(epic.getStartTime()).format(DATE_TIME_FORMATTER) +
                "\nВремя продолжительности всех задач Epic: " + epic.getDuration().toHours() +
                "\nВремя завершения всех подзадач в Epic: " + epic.getEndTime());
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);
        System.out.println(loadedManager);
        taskManager.getPrioritizedTasks().forEach(System.out::println);
    }

    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.loadFromFile();
        return manager;
    }

    public void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,type,name,status,description,epic,startDate,duration\n");
            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении файла", e);
        }
    }

    private String toString(Task task) {
        String type = task.getType() == TaskType.EPIC ? "EPIC" :
                (task.getType() == TaskType.SUBTASK ? "SUBTASK" : "TASK");

        String epicIdStr = "";
        if (task instanceof Subtask) {
            epicIdStr = String.valueOf(((Subtask) task).getEpicId());
        }
        return String.join(",",
                String.valueOf(task.getId()),
                type,
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                epicIdStr,
                task.getStartTime() != null ? (LocalDateTime.from(task.getStartTime()).format(DateTimeFormatter.ISO_DATE_TIME)) : "",
                String.valueOf(task.getDuration().toMinutes())
        );
    }

    private static Task fromString(String line) {
        String[] parts = line.split(",", -1);
        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String name = parts[2];
        String statusStr = parts[3];
        String description = parts[4];
        String startTime = parts[6];
        String duration = parts[7];

        TaskStatus status = TaskStatus.valueOf(statusStr);
        TaskType types = TaskType.valueOf(type);

        switch (type) {
            case "TASK":
                Task task = new Task(name, description, id, status,types);
                task.setStartTime(LocalDateTime.parse(startTime));
                task.setDuration(Duration.ofMinutes(Long.parseLong(duration)));
                taskManager.addTask(task);
                return task;
            case "EPIC":
                Epic epic = new Epic(name, description, id, status,types);
                epic.setStartTime(LocalDateTime.parse(startTime));
                epic.setDuration(Duration.ofMinutes(Long.parseLong(duration)));
                taskManager.addEpic(epic);
                return epic;
            case "SUBTASK":
                int epicId = Integer.parseInt(parts[5]);
                Subtask subtask = new Subtask(name, description, id, epicId, status,types);
                subtask.setStartTime(LocalDateTime.parse(startTime));
                subtask.setDuration(Duration.ofMinutes(Long.parseLong(duration)));
                taskManager.addSubtask(subtask);
                return subtask;
            default:
                throw new IllegalArgumentException("Unknown task type: " + type);
        }
    }

    @Override
    public int addTask(Task task) {
        int id = super.addTask(task);
        save();
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int id = super.addSubtask(subtask);
        save();
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        save();
        return id;
    }

    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeSubtask(int id) {
        super.removeSubtask(id);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    private void loadFromFile() {
        this.tasks.clear();
        this.epics.clear();
        this.subtasks.clear();

        if (!file.exists() || file.length() == 0) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("id")) {
                    continue;
                }
                Task task = fromString(line);
                if ("EPIC".equals(task.getType().name())) {
                    epics.put(task.getId(), (Epic) task);
                } else if (task.getType().name().equals("SUBTASK")) {
                    subtasks.put(task.getId(), (Subtask) task);
                } else {
                    tasks.put(task.getId(), task);
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при чтении файла", e);
        }
    }

    public static class ManagerSaveException extends RuntimeException {
        public ManagerSaveException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Tasks:\n");
        for (Task task : getAllTasks()) {
            sb.append(task).append(", Дата начала: ")
                    .append(task.getStartTime().format(DATE_TIME_FORMATTER))
                    .append(", продолжительность до закрытия задачи: ")
                    .append(task.getDuration())
                    .append("\n");
        }
        sb.append("Epics:\n");
        for (Epic epic : getAllEpics()) {
            sb.append(epic)
                    .append(", Дата начала: ")
                    .append(epic.getStartTime().format(DATE_TIME_FORMATTER))
                    .append("\n");
        }
        sb.append("Subtasks:\n");
        for (Subtask subtask : getAllSubtasks()) {
            sb.append(subtask)
                    .append(", Дата начала: ")
                    .append(subtask.getStartTime().format(DATE_TIME_FORMATTER))
                    .append(", продолжительность до закрытия задачи: ")
                    .append(subtask.getDuration())
                    .append("\n");
        }
        return sb.toString();
    }
}
