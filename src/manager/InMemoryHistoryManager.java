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
        Node newNode = linkLast(taskCopy);
        nodeMap.put(taskCopy.getTaskId(), newNode);
    }

    @Override
    public void remove(int id) {
        Node node = nodeMap.remove(id);
        if (node != null) {
            removeNode(node);
        }
    }

    @Override
    public List<Task> getHistory() {
        return Collections.unmodifiableList(getTasks());
    }

    private Node linkLast(Task task) {
        Node newNode = new Node(task, tail.prev, tail);
        tail.prev.next = newNode;
        tail.prev = newNode;
        return newNode;
    }

    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    private List<Task> getTasks() {
        List<Task> result = new ArrayList<>();
        Node current = head.next;
        while (current != tail) {
            result.add(copyTask(current.task));
            current = current.next;
        }
        return result;
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