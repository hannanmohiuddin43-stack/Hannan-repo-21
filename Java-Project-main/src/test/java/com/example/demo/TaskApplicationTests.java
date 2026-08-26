package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.controller.TaskController;
import com.example.demo.services.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TaskApplicationTests {

    @Autowired
    private TaskController taskController;

    @Autowired
    private TaskService taskService;

    @Test
    void contextLoadsWithTaskBeans() {
        assertThat(taskController).isNotNull();
        assertThat(taskService).isNotNull();
    }
}
