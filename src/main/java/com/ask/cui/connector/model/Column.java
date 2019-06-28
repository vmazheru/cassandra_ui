package com.ask.cui.connector.model;

import com.datastax.driver.core.ColumnMetadata;

public class Column {

    private final String name;
    private final String type;
    private final boolean staticColumn;
    private final boolean partitionKey;
    private final boolean clusteringKey;
    
    public Column(ColumnMetadata meta, boolean partitionKey, boolean clusteringKey) {
        name = meta.getName();
        type = meta.getType().toString();
        staticColumn = meta.isStatic();
        this.partitionKey = partitionKey;
        this.clusteringKey = clusteringKey;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isStaticColumn() {
        return staticColumn;
    }

    public boolean isPartitionKey() {
        return partitionKey;
    }

    public boolean isClusteringKey() {
        return clusteringKey;
    }
    
    public String toCqlString() {
        return name + " " + type + (staticColumn ? " STATIC" : "");
    }

}
