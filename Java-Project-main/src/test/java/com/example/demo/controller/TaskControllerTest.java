package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.exceptions.DuplicateTaskException;
import com.example.demo.exceptions.InvalidTaskException;
import com.example.demo.exceptions.TaskNotFoundException;
import com.example.demo.models.Status;
import com.example.demo.models.Task;
import com.example.demo.services.TaskService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    private static Task task(Long id, String title) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription(title + " description");
        task.setStatus(Status.TODO.name());
        return task;
    }

    @Test
    void postCreatesTaskFromRequestBody() throws Exception {
        given(taskService.createTask(any())).willAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"title\":\"First\",\"description\":\"d\",\"status\":\"TODO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("First"));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).createTask(captor.capture());
        Task created = captor.getValue();
        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getTitle()).isEqualTo("First");
        assertThat(created.getDescription()).isEqualTo("d");
        assertThat(created.getStatus()).isEqualTo("TODO");
    }

    @Test
    void postReturnsConflictForDuplicateId() throws Exception {
        given(taskService.createTask(any())).willThrow(new DuplicateTaskException(1L));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"title\":\"First\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Task with id 1 already exists"));
    }

    @Test
    void postReturnsBadRequestForInvalidTask() throws Exception {
        given(taskService.createTask(any())).willThrow(new InvalidTaskException("Task title is required"));

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Task title is required"));
    }

    @Test
    void postRejectsMissingBody() throws Exception {
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"));
        verify(taskService, never()).createTask(any());
    }

    @Test
    void getByIdReturnsTaskAsJson() throws Exception {
        given(taskService.getTask(1L)).willReturn(task(1L, "First"));

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("First"))
                .andExpect(jsonPath("$.description").value("First description"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void getByIdReturnsNotFoundForUnknownTask() throws Exception {
        given(taskService.getTask(404L)).willThrow(new TaskNotFoundException(404L));

        mockMvc.perform(get("/tasks/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Task with id 404 was not found"));
    }

    @Test
    void getByIdRejectsNonNumericId() throws Exception {
        mockMvc.perform(get("/tasks/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"));
    }

    @Test
    void getAllReturnsEveryTask() throws Exception {
        given(taskService.getAllTasks()).willReturn(List.of(task(1L, "First"), task(2L, "Second")));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("First"))
                .andExpect(jsonPath("$[1].title").value("Second"));
    }

    @Test
    void getAllReturnsEmptyArrayWhenNoTasks() throws Exception {
        given(taskService.getAllTasks()).willReturn(List.of());

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void putUpdatesTaskUsingPathId() throws Exception {
        given(taskService.updateTask(eq(9L), any())).willAnswer(invocation -> invocation.getArgument(1));

        mockMvc.perform(put("/tasks/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":9,\"title\":\"Updated\",\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).updateTask(eq(9L), captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Updated");
        assertThat(captor.getValue().getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void putReturnsNotFoundForUnknownTask() throws Exception {
        given(taskService.updateTask(eq(9L), any())).willThrow(new TaskNotFoundException(9L));

        mockMvc.perform(put("/tasks/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task with id 9 was not found"));
    }

    @Test
    void putRejectsMissingBody() throws Exception {
        mockMvc.perform(put("/tasks/9").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).updateTask(any(), any());
    }

    @Test
    void deleteRemovesTask() throws Exception {
        mockMvc.perform(delete("/tasks/3")).andExpect(status().isNoContent());

        verify(taskService).deleteTask(3L);
    }

    @Test
    void deleteReturnsNotFoundForUnknownTask() throws Exception {
        willThrow(new TaskNotFoundException(3L)).given(taskService).deleteTask(3L);

        mockMvc.perform(delete("/tasks/3"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task with id 3 was not found"));
    }

    @Test
    void unexpectedServiceFailureIsReportedAsServerError() throws Exception {
        given(taskService.getAllTasks()).willThrow(new IllegalStateException("boom"));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }

    @Test
    void unknownMethodOnCollectionIsRejected() throws Exception {
        mockMvc.perform(delete("/tasks")).andExpect(status().isMethodNotAllowed());
    }
}
