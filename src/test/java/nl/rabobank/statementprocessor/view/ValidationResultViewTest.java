package nl.rabobank.statementprocessor.view;

import nl.rabobank.statementprocessor.model.Record;
import nl.rabobank.statementprocessor.model.Result;
import nl.rabobank.statementprocessor.model.ValidationResult;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidationResultViewTest {

    private static final Record RECORD1 = Record.builder()
            .transactionReference(123456789)
            .accountNumber("NL34RABO0123456789")
            .startBalance(1234)
            .mutation(34)
            .endBalance(900)
            .description("Groceries")
            .build();

    private static final Record RECORD2 = Record.builder()
            .transactionReference(123456789)
            .accountNumber("NL34RABO0111111111")
            .startBalance(1111)
            .mutation(11)
            .endBalance(1100)
            .description("Something else")
            .build();

    private static final ValidationResult VALIDATION_RESULT = ValidationResult.builder()
            .result(Result.DUPLICATE_REFERENCE_INCORRECT_END_BALANCE)
            .errorRecords(new ArrayList<>(Arrays.asList(RECORD1, RECORD2)))
            .build();


    @Test
    void whenCreatingAValidationResultViewObject_shouldContainTheSameResult() {
        ValidationResultView validationResultView = new ValidationResultView(VALIDATION_RESULT);

        assertEquals(VALIDATION_RESULT.getResult(), validationResultView.getResult());
    }

    @Test
    void whenCreatingAValidationResultViewObject_shouldContainTheSameErrorRecords() {
        ValidationResultView validationResultView = new ValidationResultView(VALIDATION_RESULT);
        List<RecordView> recordViews = validationResultView.getErrorRecords();
        List<Record> records = VALIDATION_RESULT.getErrorRecords();

        assertTrue(recordViewListEqualsRecordList(recordViews, records));
    }

    private boolean recordViewListEqualsRecordList(List<RecordView> recordViews, List<Record> records) {
        if(recordViews.size() != records.size()) {
            return false;
        }
        for(Record record : records) {
            RecordView recordView = new RecordView(record.getTransactionReference(), record.getAccountNumber());
            if(!recordViews.contains(recordView)) {
                return false;
            }
        }
        return true;
    }
}