package nl.rabobank.statementprocessor.controllers;

import com.google.gson.Gson;
import nl.rabobank.statementprocessor.model.Record;
import nl.rabobank.statementprocessor.model.Result;
import nl.rabobank.statementprocessor.model.Statement;
import nl.rabobank.statementprocessor.model.ValidationResult;
import nl.rabobank.statementprocessor.services.ValidationService;
import nl.rabobank.statementprocessor.view.ValidationResultView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class StatementControllerTest {

    private static final String URI = "/statement/validate";

    private static final String INPUT_HAPPY_FLOW = "{\n" +
            "  \"records\": [\n" +
            "      {\n" +
            "          \"transactionReference\": 123456789,\n" +
            "          \"accountNumber\": \"NL34RABO0123456789\",\n" +
            "          \"startBalance\": 1234,\n" +
            "          \"mutation\": 34,\n" +
            "          \"description\": \"Groceries\",\n" +
            "          \"endBalance\": 1200 \n" +
            "      },\n" +
            "      {\n" +
            "          \"transactionReference\": 111111111,\n" +
            "          \"accountNumber\": \"NL34RABO0111111111\",\n" +
            "          \"startBalance\": 1111,\n" +
            "          \"mutation\": 11,\n" +
            "          \"description\": \"Something else\",\n" +
            "          \"endBalance\": 1100 \n" +
            "      }\n" +
            "  ]\n" +
            "}";

    private static final String INPUT_DUPLICATE_REFERENCES = "{\n" +
            "  \"records\": [\n" +
            "      {\n" +
            "          \"transactionReference\": 123456789,\n" +
            "          \"accountNumber\": \"NL34RABO0123456789\",\n" +
            "          \"startBalance\": 1234,\n" +
            "          \"mutation\": 34,\n" +
            "          \"description\": \"Groceries\",\n" +
            "          \"endBalance\": 1200 \n" +
            "      },\n" +
            "      {\n" +
            "          \"transactionReference\": 123456789,\n" +
            "          \"accountNumber\": \"NL34RABO0111111111\",\n" +
            "          \"startBalance\": 1111,\n" +
            "          \"mutation\": 11,\n" +
            "          \"description\": \"Something else\",\n" +
            "          \"endBalance\": 1100 \n" +
            "      }\n" +
            "  ]\n" +
            "}";

    // The first curly brace is missing here
    private static final String INPUT_JSON_PARSING_ERROR = "  \"records\": [\n" +
            "      {\n" +
            "          \"transactionReference\": 123456789,\n" +
            "          \"accountNumber\": \"NL34RABO0123456789\",\n" +
            "          \"startBalance\": 1234,\n" +
            "          \"mutation\": 34,\n" +
            "          \"description\": \"Groceries\",\n" +
            "          \"endBalance\": 1200 \n" +
            "      }" +
            "  ]\n" +
            "}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ValidationService validationService;

    @Test
    void shouldReturn200_whenValidationIsExecuted() throws Exception {
        ValidationResult validationResult = new ValidationResult(Result.SUCCESSFUL, new ArrayList<>());
        doReturn(validationResult).when(validationService).validateStatement(isA(Statement.class));

        this.mockMvc.perform(post(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(INPUT_HAPPY_FLOW))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnValidationResult_whenControllerIsCalled() throws Exception {
        ValidationResult validationResult = new ValidationResult(Result.SUCCESSFUL, new ArrayList<>());
        doReturn(validationResult).when(validationService).validateStatement(isA(Statement.class));

        ValidationResultView validationResultView = new ValidationResultView(validationResult);
        String expectedContent = new Gson().toJson(validationResultView);

        MvcResult mvcResult = this.mockMvc.perform(post(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(INPUT_HAPPY_FLOW))
                .andReturn();
        assertEquals(expectedContent, mvcResult.getResponse().getContentAsString());
    }

    @Test
    void shouldReturnErrorRecords_whenControllerIsCalled() throws Exception {
        Record errorRecord = Record.builder()
                .transactionReference(123456789)
                .accountNumber("NL34RABO0111111111")
                .startBalance(1111)
                .mutation(11)
                .description("Something else")
                .endBalance(1100)
                .build();
        ValidationResult validationResult = new ValidationResult(Result.DUPLICATE_REFERENCE, Arrays.asList(errorRecord));
        doReturn(validationResult).when(validationService).validateStatement(isA(Statement.class));

        ValidationResultView validationResultView = new ValidationResultView(validationResult);
        String expectedContent = new Gson().toJson(validationResultView);

        MvcResult mvcResult = this.mockMvc.perform(post(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(INPUT_DUPLICATE_REFERENCES))
                .andReturn();
        assertEquals(expectedContent, mvcResult.getResponse().getContentAsString());
    }

    /* Tests that also include the behaviour of the ControllerExceptionHandler */

    @Test
    void shouldReturn400_whenJsonSyntaxExceptionOccurs() throws Exception {
        this.mockMvc.perform(post(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(INPUT_JSON_PARSING_ERROR))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn500_whenOtherExceptionsOccur() throws Exception {
        when(validationService.validateStatement(isA(Statement.class)))
                .thenThrow(new RuntimeException("Error occurred"));

        this.mockMvc.perform(post(URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(INPUT_DUPLICATE_REFERENCES))
                .andExpect(status().isInternalServerError());
    }
}