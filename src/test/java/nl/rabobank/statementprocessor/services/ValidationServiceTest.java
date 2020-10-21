package nl.rabobank.statementprocessor.services;

import nl.rabobank.statementprocessor.model.Record;
import nl.rabobank.statementprocessor.model.Result;
import nl.rabobank.statementprocessor.model.Statement;
import nl.rabobank.statementprocessor.model.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ValidationService.class })
class ValidationServiceTest {

    private static final Record RECORD_CORRECT_123 = Record.builder()
            .transactionReference(123456789)
            .accountNumber("NL34RABO0123456789")
            .startBalance(1234)
            .mutation(34)
            .endBalance(1200)
            .description("Groceries")
            .build();

    private static final Record RECORD_CORRECT_111 = Record.builder()
            .transactionReference(111111111)
            .accountNumber("NL34RABO0111111111")
            .startBalance(1111)
            .mutation(11)
            .endBalance(1100)
            .description("Something else")
            .build();

    private static final Record RECORD_DUPLICATE_123 = Record.builder()
            .transactionReference(123456789)
            .accountNumber("NL34RABO0987654321")
            .startBalance(5678)
            .mutation(8)
            .endBalance(5670)
            .description("Clothes")
            .build();

    private static final Record RECORD_SECOND_DUPLICATE_123 = Record.builder()
            .transactionReference(123456789)
            .accountNumber("NL34RABO0987612345")
            .startBalance(3456)
            .mutation(6)
            .endBalance(3450)
            .description("Plant")
            .build();

    private static final Record RECORD_INCORRECT_ENDBALANCE_123 = Record.builder()
            .transactionReference(123456789)
            .accountNumber("NL34RABO0123456789")
            .startBalance(1234)
            .mutation(34)
            .endBalance(1100)
            .description("Groceries")
            .build();

    private static final Record RECORD_INCORRECT_ENDBALANCE_111 = Record.builder()
            .transactionReference(111111111)
            .accountNumber("NL34RABO0111111111")
            .startBalance(1111)
            .mutation(11)
            .endBalance(900)
            .description("Something else")
            .build();

    private static final Record RECORD_DUPLICATE_AND_INCORRECT_ENDBALANCE_123 = Record.builder()
            .transactionReference(123456789)
            .accountNumber("NL34RABO0123456789")
            .startBalance(1234)
            .mutation(34)
            .endBalance(1100)
            .description("Groceries")
            .build();

    @Autowired
    private ValidationService validationService;

    @Test
    void shouldReturnSuccessful_whenBothRecordsAreCorrect() {
        List<Record> records = Arrays.asList(RECORD_CORRECT_123, RECORD_CORRECT_111);
        Statement statement = new Statement(records);
        ValidationResult validationResult = validationService.validateStatement(statement);

        assertEquals(Result.SUCCESSFUL, validationResult.getResult());
    }

    @Test
    void shouldNotReturnErrorRecords_whenBothRecordsAreCorrect() {
        List<Record> records = Arrays.asList(RECORD_CORRECT_123, RECORD_CORRECT_111);
        Statement statement = new Statement(records);
        ValidationResult validationResult = validationService.validateStatement(statement);

        assertEquals(0, validationResult.getErrorRecords().size());
    }

    @Test
    void shouldReturnDuplicateReference_whenThereIsADuplicateReference() {
        List<Record> records = Arrays.asList(RECORD_CORRECT_123, RECORD_DUPLICATE_123);
        Statement statement = new Statement(records);
        ValidationResult validationResult = validationService.validateStatement(statement);

        assertEquals(Result.DUPLICATE_REFERENCE, validationResult.getResult());
    }

    @Test
    void shouldReturnErrorRecord_whenThereIsADuplicateReference() {
        List<Record> records = Arrays.asList(RECORD_CORRECT_123, RECORD_DUPLICATE_123);
        Statement statement = new Statement(records);
        List<Record> errorRecords = validationService.validateStatement(statement).getErrorRecords();

        assertEquals(1, errorRecords.size());
        assertTrue(errorRecords.contains(RECORD_DUPLICATE_123));
    }

    @Test
    void shouldReturnBothErrorRecords_whenThereAreTwoDuplicateReferences() {
        List<Record> records = Arrays.asList(RECORD_CORRECT_123, RECORD_DUPLICATE_123, RECORD_SECOND_DUPLICATE_123);
        Statement statement = new Statement(records);
        List<Record> errorRecords = validationService.validateStatement(statement).getErrorRecords();

        assertEquals(2, errorRecords.size());
        assertTrue(errorRecords.contains(RECORD_DUPLICATE_123));
        assertTrue(errorRecords.contains(RECORD_SECOND_DUPLICATE_123));
    }

    @Test
    void shouldReturnIncorrectEndBalance_whenARecordHasIncorrectEndBalance() {
        List<Record> records = Arrays.asList(RECORD_CORRECT_123, RECORD_INCORRECT_ENDBALANCE_111);
        Statement statement = new Statement(records);
        ValidationResult validationResult = validationService.validateStatement(statement);

        assertEquals(Result.INCORRECT_END_BALANCE, validationResult.getResult());
    }

    @Test
    void shouldReturnErrorRecord_whenARecordHasIncorrectEndBalance() {
        List<Record> records = Arrays.asList(RECORD_CORRECT_123, RECORD_INCORRECT_ENDBALANCE_111);
        Statement statement = new Statement(records);
        List<Record> errorRecords = validationService.validateStatement(statement).getErrorRecords();

        assertEquals(1, errorRecords.size());
        assertTrue(errorRecords.contains(RECORD_INCORRECT_ENDBALANCE_111));
    }

    @Test
    void shouldReturnBothErrorRecords_whenTwoRecordsHaveIncorrectEndBalances() {
        List<Record> records = Arrays.asList(RECORD_INCORRECT_ENDBALANCE_123, RECORD_INCORRECT_ENDBALANCE_111);
        Statement statement = new Statement(records);
        List<Record> errorRecords = validationService.validateStatement(statement).getErrorRecords();

        assertEquals(2, errorRecords.size());
        assertTrue(errorRecords.contains(RECORD_INCORRECT_ENDBALANCE_123));
        assertTrue(errorRecords.contains(RECORD_INCORRECT_ENDBALANCE_111));
    }

    @Test
    void shouldReturnDuplicateAndIncorrect_whenBothIncorrectAndDuplicateRecord_inDifferentRecords() {
        List<Record> records = Arrays.asList(RECORD_INCORRECT_ENDBALANCE_111, RECORD_CORRECT_111);
        Statement statement = new Statement(records);
        ValidationResult validationResult = validationService.validateStatement(statement);

        assertEquals(Result.DUPLICATE_REFERENCE_INCORRECT_END_BALANCE, validationResult.getResult());
    }

    @Test
    void shouldReturnBothErrorRecords_whenBothIncorrectAndDuplicateRecord_inDifferentRecords() {
        List<Record> records = Arrays.asList(RECORD_INCORRECT_ENDBALANCE_111, RECORD_CORRECT_111);
        Statement statement = new Statement(records);
        List<Record> errorRecords = validationService.validateStatement(statement).getErrorRecords();

        assertEquals(2, errorRecords.size());
        assertTrue(errorRecords.contains(RECORD_CORRECT_111));
        assertTrue(errorRecords.contains(RECORD_INCORRECT_ENDBALANCE_111));
    }

    @Test
    void shouldReturnDuplicateAndIncorrect_whenBothIncorrectAndDuplicateRecord_inSameRecord() {
        List<Record> records = Arrays.asList(RECORD_CORRECT_123, RECORD_DUPLICATE_AND_INCORRECT_ENDBALANCE_123);
        Statement statement = new Statement(records);
        ValidationResult validationResult = validationService.validateStatement(statement);

        assertEquals(Result.DUPLICATE_REFERENCE_INCORRECT_END_BALANCE, validationResult.getResult());
    }

    @Test
    void shouldReturnErrorRecord_whenBothIncorrectAndDuplicateRecord_inSameRecord() {
        List<Record> records = Arrays.asList(RECORD_CORRECT_123, RECORD_DUPLICATE_AND_INCORRECT_ENDBALANCE_123);
        Statement statement = new Statement(records);
        List<Record> errorRecords = validationService.validateStatement(statement).getErrorRecords();

        assertEquals(1, errorRecords.size());
        assertTrue(errorRecords.contains(RECORD_DUPLICATE_AND_INCORRECT_ENDBALANCE_123));
    }
}