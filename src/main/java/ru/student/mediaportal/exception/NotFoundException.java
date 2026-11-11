package ru.student.mediaportal.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String type, Long id) {
        super(type + " с id=" + id + " не найден");
    }
}
