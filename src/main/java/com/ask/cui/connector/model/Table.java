package com.ask.cui.connector.model;

import static java.util.stream.Collectors.*;

import com.datastax.driver.core.TableMetadata;

public final class Table extends AbstractTable {

    public Table(TableMetadata meta) {
        super(meta);
    }
    
    // TODO: append table options
    // TODO: implement clustering order
    @Override
    public String toCreateCql() {
        StringBuilder sb = new StringBuilder();
        String newLine = System.lineSeparator();
        String tab = "  ";
        
        sb.append("CREATE TABLE " + getName() + " (" + newLine);
        
        for (Column c : getColumns()) {
            sb.append(tab).append(c.getName()).append(" ").append(c.getType().toUpperCase()).append(",").append(newLine);
        }
        
        sb.append(tab).append("PRIMARY KEY ((");
        sb.append(String.join(", ", getPartitionKeyColumns().stream().map(c -> c.getName()).collect(toList())));
        sb.append(")");
        
        if (!getClusteringKeyColumns().isEmpty()) {
            sb.append(", ")
              .append(String.join(", ", getClusteringKeyColumns().stream().map(c -> c.getName()).collect(toList())));
        }
        sb.append(")");
        
        sb.append(newLine + ");");
        
        return sb.toString();
    }
}
