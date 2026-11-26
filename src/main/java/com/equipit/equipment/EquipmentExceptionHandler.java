package com.equipit.equipment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class EquipmentExceptionHandler {

    @ExceptionHandler(EquipmentNotFoundException.class)
    public ProblemDetail handleNotFound(EquipmentNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(EquipmentStateException.class)
    public ProblemDetail handleInvalidState(EquipmentStateException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }
}
