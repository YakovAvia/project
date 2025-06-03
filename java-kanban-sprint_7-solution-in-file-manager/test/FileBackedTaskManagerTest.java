package service.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.impl.FileBackedTaskManager;
import task.*;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackedTaskManagerTest {

    private File file;
    private FileBackedTaskManager manager;

    @BeforeEach
    public void setUp() {
        file = new File("test.csv");
        if (file.exists()) {
            file.delete();
        }
        manager = new FileBackedTaskManager(file);
    }

    @AfterEach
    public void tearDown() {

        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testSaveTaskEpicSubtask() throws IOException {

        Task task = new Task("Task", "Описание Task", 1, TaskStatus.NEW, TaskType.TASK);
        Epic epic = new Epic("Epic Task", "Описание Epic", 2, TaskStatus.NEW, TaskType.EPIC);
        Subtask subtask = new Subtask("Subtask Task", "Описание Subtask", 3, epic.getId(), TaskStatus.NEW, TaskType.SUBTASK);

        manager.addTask(task);
        manager.addEpic(epic);
        manager.addSubtask(subtask);

        manager.save();

        assertTrue(file.exists(), "Файл не был создан");
        assertTrue(file.length() > 0, "Файл пустой, данные не сохранены");

        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
        assertTrue(content.contains("Task"), "Файл не содержит задачу");
        assertTrue(content.contains("Epic Task"), "Файл не содержит эпика");
        assertTrue(content.contains("Subtask Task"), "Файл не содержит сабтаск");
        assertTrue(content.contains("2"), "Эпик ID не сохранен в сабтаске");
    }

    @Test
    public void testLoadFromFile() throws Exception {
        String data = "1,TASK,Task,NEW,Описание Task,\n" +
                "2,EPIC,Epic,NEW,Описание Epic,\n" +
                "3,SUBTASK,Subtask,NEW,Описание Subtask,2\n";
        java.nio.file.Files.writeString(file.toPath(), data);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        assertEquals(1, loadedManager.getAllTasks().size());
        assertEquals(1, loadedManager.getAllEpics().size());
        assertEquals(1, loadedManager.getAllSubtasks().size());

        Task loadedTask = loadedManager.getAllTasks().iterator().next();
        Epic loadedEpic = loadedManager.getAllEpics().iterator().next();
        Subtask loadedSubtask = loadedManager.getAllSubtasks().iterator().next();

        assertEquals("Task", loadedTask.getName());
        assertEquals("Epic", loadedEpic.getName());
        assertEquals("Subtask", loadedSubtask.getName());

        assertEquals(loadedEpic.getId(), loadedSubtask.getEpicId());
        assertEquals("Описание Task", loadedTask.getDescription());
        assertEquals("Описание Epic", loadedEpic.getDescription());
        assertEquals("Описание Subtask", loadedSubtask.getDescription());
    }

    @Test
    public void testSaveAndLoadConsistency() throws Exception {
        Task task = new Task("Task", "Описание Task", 10, TaskStatus.NEW, TaskType.TASK);
        Epic epic = new Epic("Epic", "Epic description", 20, TaskStatus.IN_PROGRESS, TaskType.EPIC);
        Subtask subtask = new Subtask("Subtask", "Subtask description", 30, epic.getId(), TaskStatus.DONE, TaskType.SUBTASK);

        manager.addTask(task);
        manager.addEpic(epic);
        manager.addSubtask(subtask);

        manager.save();

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(file);

        Task loadedTask = loadedManager.getAllTasks().iterator().next();
        Epic loadedEpic = loadedManager.getAllEpics().iterator().next();
        Subtask loadedSubtask = loadedManager.getAllSubtasks().iterator().next();

        assertEquals(task.getName(), loadedTask.getName());
        assertEquals(epic.getName(), loadedEpic.getName());
        assertEquals(subtask.getName(), loadedSubtask.getName());

        assertEquals(task.getId(), loadedTask.getId());
        assertEquals(epic.getId(), loadedEpic.getId());
        assertEquals(subtask.getId(), loadedSubtask.getId());

        assertEquals(epic.getId(), loadedSubtask.getEpicId());
        assertEquals(TaskStatus.DONE, loadedSubtask.getStatus());
    }
}