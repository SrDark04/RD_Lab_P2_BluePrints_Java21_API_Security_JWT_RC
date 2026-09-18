package co.edu.eci.blueprints.controllers;

public record ApiResponse<T>(int code, String message, T data) {
}
