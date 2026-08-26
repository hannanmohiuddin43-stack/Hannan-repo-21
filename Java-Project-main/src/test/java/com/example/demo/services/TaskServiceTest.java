package com.example.demo.services;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.models.Status;
import com.example.demo.models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskServiceTest {

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskService();
    }

    private static Task task(Long id, String title) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription(title + " description");
        task.setStatus(Status.TODO.name());
        return task;
    }

    @Test
    void createdTaskCanBeRetrievedById() {
        Task task = task(1L, "First");

        taskService.createTask(task);

        assertThat(taskService.getTask(1L)).isSameAs(task);
    }

    @Test
    void creatingTaskWithExistingIdReplacesIt() {
        taskService.createTask(task(1L, "First"));
        Task replacement = task(1L, "Replacement");

        taskService.createTask(replacement);

        assertThat(taskService.getTask(1L)).isSameAs(replacement);
        assertThat(taskService.getAllTasks()).hasSize(1);
    }

    @Test
    void createdTaskIsKeyedByItsOwnId() {
        Task task = task(42L, "Keyed");

        taskService.createTask(task);

        assertThat(taskService.getTask(99L)).isNull();
        assertThat(taskService.getTask(42L)).isSameAs(task);
    }

    @Test
    void getTaskReturnsNullForUnknownId() {
        assertThat(taskService.getTask(404L)).isNull();
    }

    @Test
    void getAllTasksIsEmptyInitially() {
        assertThat(taskService.getAllTasks()).isEmpty();
    }

    @Test
    void getAllTasksReturnsEveryStoredTask() {
        Task first = task(1L, "First");
        Task second = task(2L, "Second");
        taskService.createTask(first);
        taskService.createTask(second);

        assertThat(taskService.getAllTasks()).containsExactlyInAnyOrder(first, second);
    }

    @Test
    void getAllTasksReturnsDetachedCopy() {
        taskService.createTask(task(1L, "First"));

        taskService.getAllTasks().clear();

        assertThat(taskService.getAllTasks()).hasSize(1);
    }

    @Test
    void updateTaskReplacesStoredTaskUnderRequestedId() {
        taskService.createTask(task(1L, "Original"));
        Task updated = task(1L, "Updated");
        updated.setStatus(Status.COMPLETED.name());

        taskService.updateTask(1L, updated);

        assertThat(taskService.getTask(1L)).isSameAs(updated);
        assertThat(taskService.getTask(1L).getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void updateTaskCreatesEntryForUnknownId() {
        Task task = task(5L, "Created by update");

        taskService.updateTask(5L, task);

        assertThat(taskService.getTask(5L)).isSameAs(task);
    }

    @Test
    void updateTaskUsesPathIdRatherThanBodyId() {
        Task body = task(2L, "Body id differs");

        taskService.updateTask(1L, body);

        assertThat(taskService.getTask(1L)).isSameAs(body);
        assertThat(taskService.getTask(2L)).isNull();
    }

    @Test
    void deleteTaskRemovesStoredTask() {
        taskService.createTask(task(1L, "First"));

        taskService.deleteTask(1L);

        assertThat(taskService.getTask(1L)).isNull();
        assertThat(taskService.getAllTasks()).isEmpty();
    }

    @Test
    void deleteTaskIsNoOpForUnknownId() {
        Task task = task(1L, "First");
        taskService.createTask(task);

        taskService.deleteTask(2L);

        assertThat(taskService.getAllTasks()).containsExactly(task);
    }
}
