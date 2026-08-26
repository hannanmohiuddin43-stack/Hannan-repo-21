package com.example.demo.exceptions;

public class InvalidTaskException extends RuntimeException {

    public InvalidTaskException(String message) {
        super(message);
    }
}
