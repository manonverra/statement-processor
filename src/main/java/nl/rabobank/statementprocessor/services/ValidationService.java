package nl.rabobank.statementprocessor.services;

import lombok.extern.slf4j.Slf4j;
import nl.rabobank.statementprocessor.model.Record;
import nl.rabobank.statementprocessor.model.Result;
import nl.rabobank.statementprocessor.model.Statement;
import nl.rabobank.statementprocessor.model.ValidationResult;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ValidationService {

    public ValidationResult validateStatement(final Statement statement) {
        // Default result
        ValidationResult validationResult = new ValidationResult(Result.SUCCESSFUL, new ArrayList<>());

        // Check for duplicate references
        List<Record> duplicateReferences = findDuplicateReferences(statement.getRecords());
        boolean hasDuplicateReferences = !duplicateReferences.isEmpty();

        // Check for incorrect end balances
        List<Record> recordsWithIncorrectBalance = findRecordsWithIncorrectEndBalance(statement.getRecords());
        boolean hasIncorrectEndBalances = !recordsWithIncorrectBalance.isEmpty();

        // Determine Result
        if (hasIncorrectEndBalances && hasDuplicateReferences) {
            validationResult.setResult(Result.DUPLICATE_REFERENCE_INCORRECT_END_BALANCE);
        } else if (hasIncorrectEndBalances) {
            validationResult.setResult(Result.INCORRECT_END_BALANCE);
        } else if (hasDuplicateReferences) {
            validationResult.setResult(Result.DUPLICATE_REFERENCE);
        }

        // Determine list of error records
        // Use Set to avoid duplicates (e.g. a record could both be a duplicate reference and have an incorrect balance)
        if(hasIncorrectEndBalances || hasDuplicateReferences) {
            Set<Record> errorRecords = new HashSet<>(duplicateReferences);
            errorRecords.addAll(recordsWithIncorrectBalance);
            validationResult.setErrorRecords(new ArrayList<>(errorRecords));
        }

        return validationResult;
    }

    private List<Record> findRecordsWithIncorrectEndBalance(final List<Record> records) {
        List<Record> errorRecords = new ArrayList<>();
        for (Record record : records) {
            if (!record.validateEndBalance()) {
                errorRecords.add(record);
            }
        }
        return errorRecords;
    }

    private List<Record> findDuplicateReferences(final List<Record> records) {
        Set<Long> uniqueRecords = new HashSet<>();
        return records.stream()
                .filter(record -> !uniqueRecords.add(record.getTransactionReference()))
                .collect(Collectors.toList());
    }
}
