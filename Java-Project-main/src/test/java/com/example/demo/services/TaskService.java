package com.example.demo.services;
import com.example.demo.common.InMemoryStore;
import com.example.demo.models.Task;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class TaskService {

    private final InMemoryStore<Long, Task> tasks = new InMemoryStore<>();

    public void createTask(Task task) {
        tasks.save(task.getId(), task);
    }

    public Task getTask(Long id) {
        return tasks.find(id);
    }

    public List<Task> getAllTasks() {
        return tasks.findAll();
    }

    public void updateTask(Long id, Task task) {
        tasks.save(id, task);
    }

    public void deleteTask(Long id) {
        tasks.delete(id);
    }
}
