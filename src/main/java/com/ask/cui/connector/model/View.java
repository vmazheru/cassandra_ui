package com.ask.cui.connector.model;

import static java.util.stream.Collectors.*;

import com.datastax.driver.core.MaterializedViewMetadata;

public class View extends AbstractTable {

    private final String baseTable;
    
    public View(MaterializedViewMetadata meta) {
        super(meta);
        baseTable = meta.getBaseTable().getName();
    }
    
    public String getBaseTable() {
        return baseTable;
    }
    
    //TODO: append view options
    //TODO: implement clustering order
    @Override
    public String toCreateCql() {
        StringBuilder sb = new StringBuilder();
        String newLine = System.lineSeparator();

        sb.append("CREATE MATERIALIZED VIEW " + getName() + " AS" + newLine);
        sb.append("SELECT ");
        sb.append(String.join(", ", getColumns().stream().map(c -> c.getName()).collect(toList())));
        sb.append(newLine);
        sb.append("FROM " + baseTable + newLine);
        sb.append("WHERE ");
        sb.append(String.join(" IS NOT NULL" + newLine + "  AND ", getPrimaryKeyColumns().stream().map(c -> c.getName()).collect(toList())));
        sb.append(" IS NOT NULL" + newLine);
        
        sb.append("PRIMARY KEY ((");
        sb.append(String.join(", ", getPartitionKeyColumns().stream().map(c -> c.getName()).collect(toList())));
        sb.append(")");
        
        if (!getClusteringKeyColumns().isEmpty()) {
            sb.append(", ")
              .append(String.join(", ", getClusteringKeyColumns().stream().map(c -> c.getName()).collect(toList())));
        }
        sb.append(");");
        return sb.toString();
    }
}
