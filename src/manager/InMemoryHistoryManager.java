package manager;

import task.Task;
import java.util.*;

public class InMemoryHistoryManager implements HistoryManager {
    private static class DoublyLinkedList {
        private final Map<Integer, Node> nodeMap = new HashMap<>();
        private Node head;
        private Node tail;

        public DoublyLinkedList() {
            head = new Node(null, null, null);
            tail = new Node(null, null, null);
            head.next = tail;
            tail.prev = head;
        }

        private void linkLast(Task task) {
            Node existingNode = nodeMap.get(task.getTaskId());

            if (existingNode != null) {
                removeNode(existingNode);
            }

            Node newNode = new Node(task, tail.prev, tail);

            tail.prev.next = newNode;
            tail.prev = newNode;

            nodeMap.put(task.getTaskId(), newNode);
        }

        private List<Task> getTasks() {
            List<Task> result = new ArrayList<>();
            Node current = head.next;
            while (current != tail) {
                result.add(current.task);
                current = current.next;
            }
            return Collections.unmodifiableList(result);
        }

        private void removeNode(Node node) {
            if (node == null || node == head || node == tail) return;

            nodeMap.remove(node.task.getTaskId());

            node.prev.next = node.next;
            node.next.prev = node.prev;
        }

        private Node getNode(int id) {
            return nodeMap.get(id);
        }
    }

    private final DoublyLinkedList list = new DoublyLinkedList();

    @Override
    public void add(Task task) {
        Task taskCopy = new Task(
                task.getTaskName(),
                task.getTaskDescription(),
                task.getTaskId(),
                task.getStatus()
        );
        list.linkLast(taskCopy);
    }

    @Override
    public void remove(int id) {
        list.removeNode(list.getNode(id));
    }

    @Override
    public List<Task> getHistory() {
        return list.getTasks();
    }

    private static class Node {
        public final Task task;
        public Node prev;
        public Node next;

        public Node(Task task, Node prev, Node next) {
            this.task = task;
            this.prev = prev;
            this.next = next;
        }
    }
}