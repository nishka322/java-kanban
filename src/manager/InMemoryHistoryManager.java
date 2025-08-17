package manager;

import task.Task;
import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> nodeMap = new HashMap<>();
    private final Node head;
    private final Node tail;

    public InMemoryHistoryManager() {
        head = new Node(null);
        tail = new Node(null);
        head.next = tail;
        tail.prev = head;
    }

    @Override
    public void add(Task task) {
        if (task == null) return;

        remove(task.getTaskId());

        Task taskCopy = copyTask(task);
        Node newNode = new Node(taskCopy, tail.prev, tail);

        tail.prev.next = newNode;
        tail.prev = newNode;

        nodeMap.put(taskCopy.getTaskId(), newNode);
    }

    @Override
    public void remove(int id) {
        Node node = nodeMap.remove(id);

        if (node == null) return;

        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    @Override
    public List<Task> getHistory() {
        List<Task> result = new ArrayList<>();
        Node current = head.next;
        while (current != tail) {
            result.add(copyTask(current.task));
            current = current.next;
        }
        return Collections.unmodifiableList(result);
    }

    private Task copyTask(Task original) {
        return new Task(
                original.getTaskName(),
                original.getTaskDescription(),
                original.getTaskId(),
                original.getStatus()
        );
    }

    private static class Node {
        public final Task task;
        public Node prev;
        public Node next;

        public Node(Task task) {
            this(task, null, null);
        }

        public Node(Task task, Node prev, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }
}