package com.ask.cui.connector.model;

import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.datastax.driver.core.AbstractTableMetadata;

public abstract class AbstractTable {
    
    private final String name;
    private final String keyspace;
    private final List<Column> partitionKeyColumns;
    private final List<Column> clusteringKeyColumns;
    private final List<Column> otherColumns;
    
    //TODO: add support for indexes
    //TODO: add support for clustering column sort order
    public AbstractTable(AbstractTableMetadata meta) {
        name = meta.getName();
        keyspace = meta.getKeyspace().getName();
        partitionKeyColumns = Collections.unmodifiableList(
                meta.getPartitionKey().stream().map(c -> new Column(c, true, false)).collect(toList()));
        clusteringKeyColumns = Collections.unmodifiableList(
                meta.getClusteringColumns().stream().map(c -> new Column(c, false, true)).collect(toList()));
        
        Set<String> keyColumnNames = new HashSet<>();
        partitionKeyColumns.forEach(c -> keyColumnNames.add(c.getName()));
        clusteringKeyColumns.forEach(c -> keyColumnNames.add(c.getName()));
        
        otherColumns = Collections.unmodifiableList(meta.getColumns().stream()
            .filter(c -> !keyColumnNames.contains(c.getName()))
            .map(c -> new Column(c, false, false))
            .collect(toList()));
    }
    
    public String getName() {
        return name;
    }
    
    public String getKeyspace() {
        return keyspace;
    }
    
    public List<Column> getPartitionKeyColumns() {
        return partitionKeyColumns;
    }
    
    public List<Column> getClusteringKeyColumns() {
        return clusteringKeyColumns;
    }
    
    public List<Column> getColumns() {
        List<Column> columns = new ArrayList<>(partitionKeyColumns.size() + 
                clusteringKeyColumns.size() + otherColumns.size());
        columns.addAll(partitionKeyColumns);
        columns.addAll(clusteringKeyColumns);
        columns.addAll(otherColumns);
        return columns;
    }
    
    public List<Column> getPrimaryKeyColumns() {
        List<Column> columns = new ArrayList<>(partitionKeyColumns.size() + clusteringKeyColumns.size());
        columns.addAll(partitionKeyColumns);
        columns.addAll(clusteringKeyColumns);
        return columns;
    }

    public abstract String toCreateCql();
    
}
