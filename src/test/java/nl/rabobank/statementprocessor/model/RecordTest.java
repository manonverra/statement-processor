package nl.rabobank.statementprocessor.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RecordTest {

    private static final Record RECORD_CORRECT = Record.builder()
            .transactionReference(123456789)
            .accountNumber("NL34RABO0123456789")
            .startBalance(1234)
            .mutation(34)
            .endBalance(1200)
            .description("Groceries")
            .build();

    private static final Record RECORD_INCORRECT = Record.builder()
            .transactionReference(111111111)
            .accountNumber("NL34RABO0123456789")
            .startBalance(1234)
            .mutation(31)
            .endBalance(1200)
            .description("Groceries")
            .build();

    @Test
    void shouldReturnTrue_whenEndBalanceIsCorrect() {
        assertTrue(RECORD_CORRECT.validateEndBalance());
    }

    @Test
    void shouldReturnFalse_whenEndBalanceIsCorrect() {
        assertFalse(RECORD_INCORRECT.validateEndBalance());
    }
}