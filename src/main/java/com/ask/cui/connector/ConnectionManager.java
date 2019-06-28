package com.ask.cui.connector;

import java.util.HashMap;
import java.util.Map;

public final class ConnectionManager {
    
    private static final Map<ConnectionInfo, CassandraConnection> connectionMap = new HashMap<>();
    
    public static synchronized CassandraConnection connect(ConnectionInfo connectionInfo) {
        CassandraConnection connection = connectionMap.get(connectionInfo);
        if (connection == null) {
            connection = CassandraConnection.connect(connectionInfo);
            connectionMap.put(connectionInfo, connection);
        }
        return connection;
    }
    
    public static synchronized void disconnect(ConnectionInfo connectionInfo) {
        CassandraConnection connection = connectionMap.get(connectionInfo);
        if (connection != null) {
            connection.disconnect();
            connectionMap.remove(connectionInfo);
        }
    }
    
    public static synchronized void disconnectAll() {
        connectionMap.values().forEach(connection -> {
            try {
                connection.disconnect();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        connectionMap.clear();
    }
    
    public static synchronized boolean isConnected(ConnectionInfo connectionInfo) {
        return connectionMap.containsKey(connectionInfo);
    }

}
