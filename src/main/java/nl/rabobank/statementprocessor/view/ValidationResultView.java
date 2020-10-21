package nl.rabobank.statementprocessor.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.rabobank.statementprocessor.model.Record;
import nl.rabobank.statementprocessor.model.Result;
import nl.rabobank.statementprocessor.model.ValidationResult;

import java.util.ArrayList;
import java.util.List;


/**
 * Class that represents the 'view' of a ValidationResult that is sent back as response
 * This view is created by using RecordView objects instead of Record objects
 * This class is meant for presentation purposes only
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidationResultView {
    private Result result;
    private List<RecordView> errorRecords;

    public ValidationResultView(final ValidationResult validationResult) {
        this.result = validationResult.getResult();
        this.errorRecords = convertRecordsToRecordViews(validationResult.getErrorRecords());
    }

    private List<RecordView> convertRecordsToRecordViews(final List<Record> records) {
        List<RecordView> recordViews = new ArrayList<>();
        for(Record record : records) {
            RecordView recordView = new RecordView(record.getTransactionReference(), record.getAccountNumber());
            recordViews.add(recordView);
        }
        return recordViews;
    }
}
