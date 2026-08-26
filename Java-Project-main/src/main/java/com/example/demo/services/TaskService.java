package com.example.demo.services;
import com.example.demo.exceptions.DuplicateTaskException;
import com.example.demo.exceptions.InvalidTaskException;
import com.example.demo.exceptions.TaskNotFoundException;
import com.example.demo.models.Status;
import com.example.demo.models.Task;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class TaskService {

    private final Map<Long, Task> taskMap = new ConcurrentHashMap<>();

    public Task createTask(Task task) {
        validate(task);
        if (task.getId() == null) {
            throw new InvalidTaskException("Task id is required");
        }
        Task existing = taskMap.putIfAbsent(task.getId(), task);
        if (existing != null) {
            throw new DuplicateTaskException(task.getId());
        }
        return task;
    }

    public Task getTask(Long id) {
        Task task = taskMap.get(requireId(id));
        if (task == null) {
            throw new TaskNotFoundException(id);
        }
        return task;
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(taskMap.values());
    }

    public Task updateTask(Long id, Task task) {
        requireId(id);
        validate(task);
        if (!taskMap.containsKey(id)) {
            throw new TaskNotFoundException(id);
        }
        task.setId(id);
        taskMap.put(id, task);
        return task;
    }

    public void deleteTask(Long id) {
        if (taskMap.remove(requireId(id)) == null) {
            throw new TaskNotFoundException(id);
        }
    }

    private void validate(Task task) {
        if (task == null) {
            throw new InvalidTaskException("Task payload is required");
        }
        if (task.getTitle() == null || task.getTitle().isBlank()) {
            throw new InvalidTaskException("Task title is required");
        }
        if (task.getStatus() != null && !isKnownStatus(task.getStatus())) {
            throw new InvalidTaskException("Unknown status '" + task.getStatus()
                    + "'. Valid values are " + Arrays.toString(Status.values()));
        }
    }

    private boolean isKnownStatus(String status) {
        for (Status value : Status.values()) {
            if (value.name().equals(status)) {
                return true;
            }
        }
        return false;
    }

    private Long requireId(Long id) {
        if (id == null) {
            throw new InvalidTaskException("Task id is required");
        }
        return id;
    }
}
