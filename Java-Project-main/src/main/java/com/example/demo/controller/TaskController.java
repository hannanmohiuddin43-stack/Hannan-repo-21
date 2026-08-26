package com.example.demo.controller;
import com.example.demo.common.ApiMessages;
import com.example.demo.models.Task;
import com.example.demo.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private static final String RESOURCE = "Task";

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    //Creates a new taaask
    @PostMapping
    public String createTask(@RequestBody Task task) {
        taskService.createTask(task);
        return ApiMessages.created(RESOURCE);
    }

    // used to Get a task by ID
    @GetMapping("/{id}")
    public Task getTask(@PathVariable Long id) {
        return taskService.getTask(id);
    }

    // Gets all tasks
    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    // Update a task
    @PutMapping("/{id}")
    public String updateTask(@PathVariable Long id, @RequestBody Task task) {
        taskService.updateTask(id, task);
        return ApiMessages.updated(RESOURCE);
    }

    //deletes a task
    @DeleteMapping("/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ApiMessages.deleted(RESOURCE);
    }
}
