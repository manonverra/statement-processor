package nl.rabobank.statementprocessor.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Statement {
    private List<Record> records;
}
