package nl.rabobank.statementprocessor.controllers;

import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import nl.rabobank.statementprocessor.model.Result;
import nl.rabobank.statementprocessor.view.ValidationResultView;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;

@Slf4j
@ControllerAdvice(basePackages="nl.rabobank.statementprocessor")
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler
    protected ResponseEntity<ValidationResultView> handleJsonSyntaxException(final JsonSyntaxException exception) {
        log.error("JSON parsing error, exception: {}", exception.getMessage());
        ValidationResultView response = new ValidationResultView(Result.BAD_REQUEST, new ArrayList<>());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Default behaviour for Exceptions that are not handled otherwise
    @ExceptionHandler
    protected ResponseEntity<ValidationResultView> handleOtherExceptions(final Exception exception) {
        log.error("Internal server error, exception: {}", exception.getMessage());
        ValidationResultView response = new ValidationResultView(Result.INTERNAL_SERVER_ERROR, new ArrayList<>());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
