package com.example.demo.services;

import com.example.demo.common.InMemoryStore;
import com.example.demo.models.Task;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TaskService {

    private final InMemoryStore<Long, Task> tasks = new InMemoryStore<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    public Task createTask(Task task) {
        task.setId(idSequence.getAndIncrement());
        tasks.save(task.getId(), task);
        return task;
    }

    public Optional<Task> getTask(Long id) {
        return tasks.find(id);
    }

    public List<Task> getAllTasks() {
        return tasks.findAll();
    }

    public Optional<Task> updateTask(Long id, Task task) {
        task.setId(id);
        return tasks.replaceIfPresent(id, task);
    }

    public boolean deleteTask(Long id) {
        return tasks.delete(id);
    }
}
