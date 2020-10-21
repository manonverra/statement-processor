package nl.rabobank.statementprocessor.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Record {
    private long transactionReference;
    private String accountNumber;
    private String description;
    private long startBalance;
    private long mutation;
    private long endBalance;

    public boolean validateEndBalance() {
        return startBalance - endBalance == mutation;
    }
}
