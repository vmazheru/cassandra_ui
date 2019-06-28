package com.ask.cui.connector;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public final class ConnectionInfo implements Serializable {

    private static final long serialVersionUID = -3853197744705037477L;

    private final String name;
    private final String contactPoints;
    private final String username;
    private final String password;
    private final String keyspace;
    private final String jksPassword;
    private final String jksFile;
    
    public ConnectionInfo(
            String name, String contactPoints, String username, String password,
            String jksPassword, String jksFile,
            Optional<String> keyspace) {
        this.name = name;
        this.contactPoints = contactPoints;
        this.username = username;
        this.password = password;
        this.jksPassword = jksPassword;
        this.jksFile = jksFile;
        this.keyspace = keyspace.orElse(null);
    }

    public String getName() {
        return name;
    }

    public String getContactPoints() {
        return contactPoints;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    
    public String getJksPassword() {
        return jksPassword;
    }
    
    public String getJksFile() {
        return jksFile;
    }
    
    public Optional<String> getKeyspace() {
        return Optional.ofNullable(keyspace);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(contactPoints, username, keyspace);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (getClass() != o.getClass()) return false;
        ConnectionInfo other = (ConnectionInfo)o;
        return Objects.equals(contactPoints, other.contactPoints) && 
               Objects.equals(username, other.username) &&
               Objects.equals(keyspace, other.keyspace);
    }
}
