package com.example.app.dto;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private final String status;
    private final T data;
    private final String message;

    private ApiResponse(String status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", null, message);
    }
}