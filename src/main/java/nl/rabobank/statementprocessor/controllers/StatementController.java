package nl.rabobank.statementprocessor.controllers;

import nl.rabobank.statementprocessor.view.ValidationResultView;
import nl.rabobank.statementprocessor.services.ValidationService;
import nl.rabobank.statementprocessor.model.Statement;
import nl.rabobank.statementprocessor.model.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

@RestController
public class StatementController {

    private final ValidationService validationService;

    @Autowired
    public StatementController(final ValidationService validationService) {
        this.validationService = validationService;
    }

    @PostMapping(path = "/statement/validate", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ValidationResultView> validateStatements(@RequestBody final String json) {
        Statement statement = new Gson().fromJson(json, Statement.class);
        ValidationResult validationResult = validationService.validateStatement(statement);
        return new ResponseEntity<>(new ValidationResultView(validationResult), HttpStatus.OK);
    }
}