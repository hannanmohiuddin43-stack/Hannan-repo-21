package com.example.demo.models;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class StatusTest {

    @Test
    void declaresTheSupportedStatusesInOrder() {
        assertThat(Status.values())
                .containsExactly(Status.TODO, Status.IN_PROGRESS, Status.COMPLETED, Status.BLOCKED);
    }

    @Test
    void valueOfResolvesEachName() {
        for (Status status : Status.values()) {
            assertThat(Status.valueOf(status.name())).isSameAs(status);
        }
    }

    @Test
    void valueOfRejectsUnknownName() {
        assertThatThrownBy(() -> Status.valueOf("ARCHIVED"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
