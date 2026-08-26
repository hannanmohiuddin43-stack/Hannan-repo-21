package com.example.demo.services;

import com.example.demo.models.Task;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {

    private final ConcurrentMap<Long, Task> taskMap = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public Task createTask(Task task) {
        task.setId(idSequence.getAndIncrement());
        taskMap.put(task.getId(), task);
        return task;
    }

    public Optional<Task> getTask(Long id) {
        return Optional.ofNullable(taskMap.get(id));
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(taskMap.values());
    }

    public Optional<Task> updateTask(Long id, Task task) {
        task.setId(id);
        return Optional.ofNullable(taskMap.computeIfPresent(id, (key, value) -> task));
    }

    public boolean deleteTask(Long id) {
        return taskMap.remove(id) != null;
    }
}
