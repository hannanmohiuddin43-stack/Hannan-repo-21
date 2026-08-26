package com.example.demo.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.models.Status;
import com.example.demo.models.Task;
import com.example.demo.services.TaskService;
import com.example.demo.web.ApiExceptionHandler;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TaskController.class)
@Import(ApiExceptionHandler.class)
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
        task.setStatus(Status.TODO);
        return task;
    }

    @Test
    void postCreatesTaskFromRequestBody() throws Exception {
        Task created = task(7L, "First");
        given(taskService.createTask(any(Task.class))).willReturn(created);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":1,\"title\":\"First\",\"description\":\"d\",\"status\":\"TODO\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/tasks/7")))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.title").value("First"))
                .andExpect(jsonPath("$.description").value("First description"))
                .andExpect(jsonPath("$.status").value("TODO"));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).createTask(captor.capture());
        Task request = captor.getValue();
        assertThat(request.getId()).isNull();
        assertThat(request.getTitle()).isEqualTo("First");
        assertThat(request.getDescription()).isEqualTo("d");
        assertThat(request.getStatus()).isEqualTo(Status.TODO);
    }

    @Test
    void postRejectsMissingBody() throws Exception {
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).createTask(any());
    }

    @Test
    void postRejectsMalformedBody() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":"))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).createTask(any());
    }

    @Test
    void postRejectsBlankTitle() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\" \",\"status\":\"TODO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("must not be blank"));
        verify(taskService, never()).createTask(any());
    }

    @Test
    void postRejectsMissingTitle() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"TODO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("must not be blank"));
        verify(taskService, never()).createTask(any());
    }

    @Test
    void postRejectsOverlongTitle() throws Exception {
        String title = "a".repeat(201);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"" + title + "\",\"status\":\"TODO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("size must be between 0 and 200"));
        verify(taskService, never()).createTask(any());
    }

    @Test
    void postRejectsUnknownStatus() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"First\",\"status\":\"UNKNOWN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid request body"));
        verify(taskService, never()).createTask(any());
    }

    @Test
    void getByIdReturnsTaskAsJson() throws Exception {
        given(taskService.getTask(1L)).willReturn(Optional.of(task(1L, "First")));

        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("First"))
                .andExpect(jsonPath("$.description").value("First description"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void getByIdReturnsNotFoundForUnknownTask() throws Exception {
        given(taskService.getTask(404L)).willReturn(Optional.empty());

        mockMvc.perform(get("/tasks/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByIdRejectsNonNumericId() throws Exception {
        mockMvc.perform(get("/tasks/abc")).andExpect(status().isBadRequest());
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
        Task updated = task(9L, "Updated");
        updated.setStatus(Status.COMPLETED);
        given(taskService.updateTask(eq(9L), any(Task.class))).willReturn(Optional.of(updated));

        mockMvc.perform(put("/tasks/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":99,\"title\":\"Updated\",\"status\":\"COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.title").value("Updated"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskService).updateTask(eq(9L), captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("Updated");
        assertThat(captor.getValue().getStatus()).isEqualTo(Status.COMPLETED);
        assertThat(captor.getValue().getId()).isNull();
    }

    @Test
    void putReturnsNotFoundForUnknownTask() throws Exception {
        given(taskService.updateTask(eq(404L), any(Task.class))).willReturn(Optional.empty());

        mockMvc.perform(put("/tasks/404")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated\",\"status\":\"TODO\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void putRejectsMissingBody() throws Exception {
        mockMvc.perform(put("/tasks/9").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(taskService, never()).updateTask(any(), any());
    }

    @Test
    void deleteRemovesTask() throws Exception {
        given(taskService.deleteTask(3L)).willReturn(true);

        mockMvc.perform(delete("/tasks/3"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(taskService).deleteTask(3L);
    }

    @Test
    void deleteReturnsNotFoundForUnknownTask() throws Exception {
        given(taskService.deleteTask(404L)).willReturn(false);

        mockMvc.perform(delete("/tasks/404"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unknownMethodOnCollectionIsRejected() throws Exception {
        mockMvc.perform(delete("/tasks"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void nonNumericIdIsRejected() throws Exception {
        mockMvc.perform(get("/tasks/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid value for parameter 'id'"));
        verify(taskService, never()).getTask(any());
    }

    @Test
    void unexpectedServiceFailureIsReportedAsServerError() throws Exception {
        given(taskService.getAllTasks()).willThrow(new IllegalStateException("boom"));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error"));
    }
}
