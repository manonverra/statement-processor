package nl.rabobank.statementprocessor.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Class that represents the 'view' of a Record that is sent back as response
 * The fields of this RecordView object are a subset of the fields of the original Record object
 * This class is meant for presentation purposes only
 */

@Data
@Builder
@AllArgsConstructor
public class RecordView {
    private long transactionReference;
    private String accountNumber;
}
