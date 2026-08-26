package com.example.demo.common;

public final class ApiMessages {

    private ApiMessages() {
    }

    public static String created(String resource) {
        return "New " + resource + " created successfully";
    }

    public static String updated(String resource) {
        return "Existing " + resource + " updated successfully";
    }

    public static String deleted(String resource) {
        return resource + " deleted successfully";
    }
}
