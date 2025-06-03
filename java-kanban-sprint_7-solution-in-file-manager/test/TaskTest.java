package service.test;

import org.junit.Assert;
import org.junit.Test;
import service.HistoryManager;
import service.Managers;
import service.TaskManager;
import service.impl.InMemoryTaskManager;
import task.*;

import java.util.List;

public class TaskTest {

    TaskManager taskManager = Managers.getDefault();
    HistoryManager historyManager = Managers.getDefaultHistory();

    //проверяю, что экземпляры класса Task равны друг другу, если равен их id
    @Test
    public void addNewTask() {
        Task task = new Task("Task", "Описание Task", 1, TaskStatus.NEW, TaskType.TASK);
        final int taskId = taskManager.addTask(task);
        final Task savedTask = taskManager.getTask(taskId);
        Assert.assertEquals("ТЕСТ1.Совпадение задач", taskId, savedTask.getId());
        Assert.assertNotNull("Задача не найдена", savedTask);
        Assert.assertEquals(taskId, savedTask.getId());
    }

    //проверяю, что наследники класса Task равны друг другу, если равен их id;
    @Test
    public void extEqualTask() {
        Epic epic = new Epic("Epic Task", "Описание Epic", 123, TaskStatus.NEW, TaskType.EPIC);
        final int epicId = taskManager.addEpic(epic);
        final Epic savedEpic = taskManager.getEpic(epicId);
        Assert.assertEquals("ТЕСТ2.Наследники класса Task должны быть равны друг другу,если равен их ID",
                epicId, savedEpic.getId());
        Subtask subtask = new Subtask("Subtask Task", "Описание Subtask Task", 1234, epicId, TaskStatus.NEW, TaskType.SUBTASK);
        final int subtaskId = taskManager.addSubtask(subtask);
        final Subtask savedSubtask = taskManager.getSubtask(subtaskId);
        Assert.assertEquals("ТЕСТ2.Наследники класса Task должны быть равны друг другу,если равен их ID",
                subtaskId, savedSubtask.getId());
    }

    //проверяю, что объект Epic нельзя добавить в самого себя в виде подзадачи;
    @Test
    public void testEpicCannotBeSubtaskOfItself() {
        Epic epic = new Epic("Epic Task", "Описание Epic", 1, TaskStatus.NEW, TaskType.EPIC);
        final int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask Task", "Описание Subtask", epicId, epicId, TaskStatus.NEW, TaskType.SUBTASK);
        final int result = taskManager.addSubtask(subtask);
        Assert.assertEquals("Epic не может быть подзадачей самой себя.", -1, result);
    }

    //проверяю, что объект Subtask нельзя сделать своим же эпиком;
    @Test
    public void testSubtaskCannotBeItsOwnEpic() {
        Epic epic = new Epic("Epic Task", "Описание Epic", 1, TaskStatus.NEW, TaskType.EPIC);
        final int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask("Subtask Task", "Описание Subtask", epicId, epicId, TaskStatus.NEW, TaskType.SUBTASK);
        final int result = taskManager.addSubtask(subtask);
        Assert.assertEquals("Подзадача не может быть эпиком самой себя.", -1, result);
    }

    //убедился, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров;
    @Test
    public void testTaskManagerInitialized() {
        TaskManager taskManager = Managers.getDefault();
        Task task = new Task("Task", "Описание Task", 123, TaskStatus.NEW, TaskType.TASK);
        Assert.assertNotNull(taskManager);
        taskManager.addTask(task);
        Assert.assertEquals("Task", task.getName());
    }

    @Test
    public void testHistoryManagerInitialized() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        Assert.assertNotNull(historyManager);
        Task task = new Task("Task", "Описание Task", 123, TaskStatus.NEW, TaskType.TASK);
        historyManager.addHistory(task);
        List<Task> history = historyManager.getHistory();
        Assert.assertNotNull("История не должна быть пустой.", history);
    }

    //проверил, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id;
    @Test
    public void testInMemoryTaskManager() {
        Task task = new Task("Task", "Описание Task", 123, TaskStatus.NEW, TaskType.TASK);
        Epic epic = new Epic("Epic Task", "Описание Epic", 1, TaskStatus.NEW, TaskType.EPIC);
        Subtask subtask = new Subtask("Subtask Task", "Описание Subtask", 1, 2, TaskStatus.NEW, TaskType.EPIC);
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        taskManager.addTask(task);
        taskManager.addEpic(epic);
        taskManager.addSubtask(subtask);

        Assert.assertEquals(task, taskManager.getTask(123));
        Assert.assertEquals(epic, taskManager.getEpic(1));
        Assert.assertEquals(subtask, taskManager.getSubtask(1));

        Assert.assertEquals(1, taskManager.getAllTasks().size());
        Assert.assertEquals(1, taskManager.getAllEpics().size());
        Assert.assertEquals(1, taskManager.getAllSubtasks().size());
    }

    //проверяю, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
    @Test
    public void testGenerateIdTask() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Task taskOne = new Task("Task", "Описание Task", 3, TaskStatus.NEW, TaskType.TASK);
        Task task = new Task("Task", "Описание Task", taskManager.generateId(), TaskStatus.NEW, TaskType.TASK);

        taskManager.addTask(task);
        taskManager.addTask(taskOne);

        Assert.assertEquals("Поиск по ID не работает", taskOne, taskManager.getTask(3));
        Assert.assertNotEquals("ID задач не должны совпадать", taskOne.getId(), task.getId());
    }

    //создал тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
    @Test
    public void addTask() {
        Task task = new Task("Task", "Описание Task", 123, TaskStatus.NEW, TaskType.TASK);
        taskManager.addTask(task);
        final Task savedTask = taskManager.getTask(task.getId());
        Assert.assertEquals("Имя должно быть одинаковым.", task.getName(), savedTask.getName());
        Assert.assertEquals("Описание должно быть одинаковым.", task.getDescription(), savedTask.getDescription());
        Assert.assertEquals("ID не должен измениться.", task.getId(), savedTask.getId());
        Assert.assertEquals("Статус должен быть NEW.", task.getStatus(), savedTask.getStatus());
    }

    //убедился, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    @Test
    public void addHistory() {
        Task task = new Task("Task", "Описание Task", 1, TaskStatus.NEW, TaskType.TASK);
        Task taskOne = new Task("Task", "Описание Task", 2, TaskStatus.NEW, TaskType.TASK);
        historyManager.addHistory(task);
        historyManager.addHistory(taskOne);
        List<Task> history = historyManager.getHistory();
        Assert.assertNotNull("История не должна быть пустой.", history);
        Assert.assertEquals("История после добавления задачи не должна быть пустой.", history.size(), 2);
        Assert.assertEquals("Первая задача в истории должна быть равна первой добавленной задаче.", task, history.get(0));
        Assert.assertEquals("Вторая задача в истории должна быть равна второй добавленной задаче.", taskOne, history.get(1));
    }
}