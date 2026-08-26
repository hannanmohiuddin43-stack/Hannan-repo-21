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
        task.setStatus(Status.TODO);
        return task;
    }

    @Test
    void createdTaskCanBeRetrievedById() {
        Task task = task(99L, "First");

        Task created = taskService.createTask(task);

        assertThat(created).isSameAs(task);
        assertThat(created.getId()).isEqualTo(1L);
        assertThat(taskService.getTask(1L)).containsSame(task);
    }

    @Test
    void creatingTasksWithSameCallerIdGetsDistinctGeneratedIds() {
        Task first = task(42L, "First");
        Task second = task(42L, "Second");

        Task createdFirst = taskService.createTask(first);
        Task createdSecond = taskService.createTask(second);

        assertThat(createdFirst.getId()).isEqualTo(1L);
        assertThat(createdSecond.getId()).isEqualTo(2L);
        assertThat(createdFirst.getId()).isNotEqualTo(createdSecond.getId());
        assertThat(taskService.getAllTasks()).hasSize(2);
    }

    @Test
    void createTaskIgnoresCallerSuppliedId() {
        Task task = task(42L, "Keyed");

        Task created = taskService.createTask(task);

        assertThat(created.getId()).isEqualTo(1L);
        assertThat(taskService.getTask(42L)).isEmpty();
        assertThat(taskService.getTask(1L)).containsSame(task);
    }

    @Test
    void getTaskReturnsEmptyForUnknownId() {
        assertThat(taskService.getTask(404L)).isEmpty();
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
        updated.setStatus(Status.COMPLETED);

        assertThat(taskService.updateTask(1L, updated)).containsSame(updated);

        assertThat(taskService.getTask(1L)).containsSame(updated);
        assertThat(taskService.getTask(1L)).hasValueSatisfying(task ->
                assertThat(task.getStatus()).isEqualTo(Status.COMPLETED));
    }

    @Test
    void updateTaskDoesNotCreateEntryForUnknownId() {
        Task task = task(5L, "Created by update");

        assertThat(taskService.updateTask(5L, task)).isEmpty();

        assertThat(taskService.getTask(5L)).isEmpty();
        assertThat(taskService.getAllTasks()).isEmpty();
    }

    @Test
    void updateTaskUsesPathIdRatherThanBodyId() {
        Task existing = task(99L, "Original");
        taskService.createTask(existing);
        Task body = task(2L, "Body id differs");

        assertThat(taskService.updateTask(1L, body)).containsSame(body);

        assertThat(taskService.getTask(1L)).containsSame(body);
        assertThat(taskService.getTask(2L)).isEmpty();
    }

    @Test
    void deleteTaskRemovesStoredTask() {
        taskService.createTask(task(1L, "First"));

        assertThat(taskService.deleteTask(1L)).isTrue();

        assertThat(taskService.getTask(1L)).isEmpty();
        assertThat(taskService.getAllTasks()).isEmpty();
    }

    @Test
    void deleteTaskIsNoOpForUnknownId() {
        Task task = task(1L, "First");
        taskService.createTask(task);

        assertThat(taskService.deleteTask(2L)).isFalse();

        assertThat(taskService.getAllTasks()).containsExactly(task);
    }
}
