import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // Проверка сеттеров Task
        Task task = new Task("OldName", "OldDesc");
        task.setTaskName("NewName");
        task.setTaskDescription("NewDesc");
        assertEquals("NewName", task.getTaskName());
        assertEquals("NewDesc", task.getTaskDescription());

        // Добавление и обновление Task
        Task addedTask = manager.addTask(new Task("T1", "D1"));
        addedTask.setTaskName("T1-upd");
        addedTask.setTaskDescription("D1-upd");
        Task updatedTask = manager.updateTask(addedTask);
        assertNotNull(updatedTask);
        assertEquals("T1-upd", manager.getTask(addedTask.getTaskId()).getTaskName());
        assertEquals("D1-upd", manager.getTask(addedTask.getTaskId()).getTaskDescription());

        // Проверка deleteTask
        Task toDelete = manager.addTask(new Task("ToDel", "D"));
        int delId = toDelete.getTaskId();
        manager.deleteTask(delId);
        assertNull(manager.getTask(delId));

        // Проверка конструктора Subtask с полными параметрами
        Epic epicTmp = manager.addEpic(new Epic("Etmp", "DescEtmp"));
        Subtask fullSub = new Subtask("Sfull", "Dfull", 99, Status.NEW, epicTmp.getTaskId());
        assertEquals(99, fullSub.getTaskId());
        assertEquals("Sfull", fullSub.getTaskName());
        assertEquals(epicTmp.getTaskId(), fullSub.getEpicId());

        // Проверка конструктора Epic с полными параметрами
        Epic fullEpic = new Epic("Efull", "DescEfull", 55, Status.NEW);
        assertEquals(55, fullEpic.getTaskId());
        assertEquals("Efull", fullEpic.getTaskName());

        // Проверка setSubtasks в Epic
        ArrayList<Subtask> customList = new ArrayList<>();
        customList.add(fullSub);
        fullEpic.setSubtasks(customList);
        assertEquals(1, fullEpic.getSubtasks().size());

        // Добавление эпика и подзадач
        Epic epic1 = manager.addEpic(new Epic("Epic1", "Desc1"));
        Subtask sub1 = manager.addSubtask(new Subtask("Sub1", "D1", epic1.getTaskId()));
        Subtask sub2 = manager.addSubtask(new Subtask("Sub2", "D2", epic1.getTaskId()));

        // Проверка getSubtask и getEpicSubtasks
        assertEquals(sub1, manager.getSubtask(sub1.getTaskId()));
        ArrayList<Subtask> epicSubs = manager.getEpicSubtasks(epic1);
        assertEquals(2, epicSubs.size());

        // Проверка updateSubtask
        sub1.setTaskName("Sub1-new");
        sub1.setStatus(Status.DONE);
        Subtask updatedSub = manager.updateSubtask(sub1);
        assertNotNull(updatedSub);
        assertEquals("Sub1-new", manager.getSubtask(sub1.getTaskId()).getTaskName());
        // После обновления одной DONE и одной NEW, эпик должен быть IN_PROGRESS
        assertEquals(Status.IN_PROGRESS, manager.getEpic(epic1.getTaskId()).getStatus());

        // Обновляем вторую подзадачу в DONE, проверка статуса эпика
        sub2.setStatus(Status.DONE);
        manager.updateSubtask(sub2);
        assertEquals(Status.DONE, manager.getEpic(epic1.getTaskId()).getStatus());

        // Проверка updateEpic
        epic1.setTaskName("Epic1-upd");
        epic1.setTaskDescription("Desc1-upd");
        // меняем подзадачи: оставляем только sub2
        ArrayList<Subtask> newList = new ArrayList<>(); newList.add(sub2);
        epic1.setSubtasks(newList);
        Epic updatedEpic = manager.updateEpic(epic1);
        assertNotNull(updatedEpic);
        assertEquals("Epic1-upd", manager.getEpic(epic1.getTaskId()).getTaskName());
        assertEquals(1, manager.getEpic(epic1.getTaskId()).getSubtasks().size());

        // Проверка deleteEpic
        Epic epic2 = manager.addEpic(new Epic("Epic2", "Desc2"));
        Subtask s21 = manager.addSubtask(new Subtask("Sub21", "D21", epic2.getTaskId()));
        int epic2Id = epic2.getTaskId();
        manager.deleteEpic(epic2Id);
        assertNull(manager.getEpic(epic2Id));
        assertNull(manager.getSubtask(s21.getTaskId()));

        // Проверка deleteSubtask
        Epic epic3 = manager.addEpic(new Epic("Epic3", "Desc3"));
        Subtask s31 = manager.addSubtask(new Subtask("Sub31", "D31", epic3.getTaskId()));
        Subtask s32 = manager.addSubtask(new Subtask("Sub32", "D32", epic3.getTaskId()));
        int s31Id = s31.getTaskId();
        manager.deleteSubtask(s31Id);
        assertNull(manager.getSubtask(s31Id));
        ArrayList<Subtask> remainingSub = manager.getEpicSubtasks(epic3);
        assertEquals(1, remainingSub.size());
        assertEquals(s32, remainingSub.getFirst());

        // Проверка clearTasks, clearSubtasks, clearEpics
        manager.clearTasks();
        assertTrue(manager.getTasks().isEmpty());

        manager.clearSubtasks();
        for (Epic e : manager.getEpics()) {
            assertTrue(e.getSubtasks().isEmpty());
            assertEquals(Status.NEW, e.getStatus());
        }

        manager.clearEpics();
        assertTrue(manager.getEpics().isEmpty());
        assertTrue(manager.getSubtasks().isEmpty());

        System.out.println("All extended tests passed.");
    }
}
