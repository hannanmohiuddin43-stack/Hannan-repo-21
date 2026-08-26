package com.example.demo.models;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TaskTest {

    @Test
    void newTaskHasNullFields() {
        Task task = new Task();

        assertThat(task.getId()).isNull();
        assertThat(task.getTitle()).isNull();
        assertThat(task.getDescription()).isNull();
        assertThat(task.getStatus()).isNull();
    }

    @Test
    void settersUpdateAllFields() {
        Task task = new Task();

        task.setId(7L);
        task.setTitle("Write tests");
        task.setDescription("Cover the task API");
        task.setStatus(Status.IN_PROGRESS.name());

        assertThat(task.getId()).isEqualTo(7L);
        assertThat(task.getTitle()).isEqualTo("Write tests");
        assertThat(task.getDescription()).isEqualTo("Cover the task API");
        assertThat(task.getStatus()).isEqualTo("IN_PROGRESS");
    }

    @Test
    void toStringContainsAllFieldValues() {
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Title");
        task.setDescription("Description");
        task.setStatus("TODO");

        assertThat(task.toString())
                .isEqualTo("Task{id=1, title='Title', description='Description', status='TODO'}");
    }

    @Test
    void toStringHandlesUnsetFields() {
        assertThat(new Task().toString())
                .isEqualTo("Task{id=null, title='null', description='null', status='null'}");
    }
}
