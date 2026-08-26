package com.example.demo.exceptions;

public class DuplicateTaskException extends RuntimeException {

    public DuplicateTaskException(Long id) {
        super("Task with id " + id + " already exists");
    }
}
