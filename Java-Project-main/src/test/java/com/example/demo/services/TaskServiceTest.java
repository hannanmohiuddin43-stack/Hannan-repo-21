package com.example.demo.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.demo.exceptions.DuplicateTaskException;
import com.example.demo.exceptions.InvalidTaskException;
import com.example.demo.exceptions.TaskNotFoundException;
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
    void creatingTaskWithExistingIdIsRejected() {
        Task original = task(1L, "First");
        taskService.createTask(original);

        assertThatThrownBy(() -> taskService.createTask(task(1L, "Replacement")))
                .isInstanceOf(DuplicateTaskException.class);
        assertThat(taskService.getTask(1L)).isSameAs(original);
        assertThat(taskService.getAllTasks()).hasSize(1);
    }

    @Test
    void creatingTaskWithoutIdIsRejected() {
        assertThatThrownBy(() -> taskService.createTask(task(null, "No id")))
                .isInstanceOf(InvalidTaskException.class);
        assertThat(taskService.getAllTasks()).isEmpty();
    }

    @Test
    void creatingTaskWithoutTitleIsRejected() {
        assertThatThrownBy(() -> taskService.createTask(task(1L, "  ")))
                .isInstanceOf(InvalidTaskException.class);
        assertThat(taskService.getAllTasks()).isEmpty();
    }

    @Test
    void creatingTaskWithUnknownStatusIsRejected() {
        Task task = task(1L, "First");
        task.setStatus("DONE");

        assertThatThrownBy(() -> taskService.createTask(task))
                .isInstanceOf(InvalidTaskException.class)
                .hasMessageContaining("DONE");
        assertThat(taskService.getAllTasks()).isEmpty();
    }

    @Test
    void createdTaskIsKeyedByItsOwnId() {
        Task task = task(42L, "Keyed");

        taskService.createTask(task);

        assertThatThrownBy(() -> taskService.getTask(99L)).isInstanceOf(TaskNotFoundException.class);
        assertThat(taskService.getTask(42L)).isSameAs(task);
    }

    @Test
    void getTaskThrowsForUnknownId() {
        assertThatThrownBy(() -> taskService.getTask(404L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("404");
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
    void updateTaskThrowsForUnknownId() {
        assertThatThrownBy(() -> taskService.updateTask(5L, task(5L, "Created by update")))
                .isInstanceOf(TaskNotFoundException.class);
        assertThat(taskService.getAllTasks()).isEmpty();
    }

    @Test
    void updateTaskUsesPathIdRatherThanBodyId() {
        taskService.createTask(task(1L, "Original"));
        Task body = task(2L, "Body id differs");

        taskService.updateTask(1L, body);

        assertThat(taskService.getTask(1L)).isSameAs(body);
        assertThat(body.getId()).isEqualTo(1L);
        assertThatThrownBy(() -> taskService.getTask(2L)).isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void deleteTaskRemovesStoredTask() {
        taskService.createTask(task(1L, "First"));

        taskService.deleteTask(1L);

        assertThatThrownBy(() -> taskService.getTask(1L)).isInstanceOf(TaskNotFoundException.class);
        assertThat(taskService.getAllTasks()).isEmpty();
    }

    @Test
    void deleteTaskThrowsForUnknownId() {
        Task task = task(1L, "First");
        taskService.createTask(task);

        assertThatThrownBy(() -> taskService.deleteTask(2L)).isInstanceOf(TaskNotFoundException.class);
        assertThat(taskService.getAllTasks()).containsExactly(task);
    }
}
