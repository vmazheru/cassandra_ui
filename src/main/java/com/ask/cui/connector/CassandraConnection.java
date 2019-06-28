package com.ask.cui.connector;

import static java.util.stream.Collectors.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Comparator;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.ask.cui.connector.model.Table;
import com.ask.cui.connector.model.View;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.PlainTextAuthProvider;
import com.datastax.driver.core.QueryOptions;
import com.datastax.driver.core.RemoteEndpointAwareJdkSSLOptions;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;

public class CassandraConnection {

    private final ConnectionInfo connectionInfo;
    private final String keyspace;
    private final Cluster cluster;
    private final Session session;
    
    private CassandraConnection(ConnectionInfo connectionInfo) {
        this.connectionInfo = connectionInfo;
        this.keyspace = connectionInfo.getKeyspace().orElseGet(() -> null);
        cluster = getCluster(connectionInfo);
        session = cluster.connect(this.keyspace);
    }
    
    static CassandraConnection connect(ConnectionInfo connectionInfo) {
        return new CassandraConnection(connectionInfo);
    }
    
    void disconnect() {
        session.close();
        cluster.close();
    }
    
    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }
    
    public List<String> getKeyspaceNames() {
        return cluster.getMetadata().getKeyspaces().stream().map(KeyspaceMetadata::getName).sorted().collect(toList());
    }
    
    public List<Table> getTables(String keyspace) {
        return cluster.getMetadata().getKeyspace(keyspace).getTables().stream()
                .map(t -> new Table(t)).sorted(Comparator.comparing(Table::getName)).collect(toList());
    }
    
    public List<Table> getTables() {
        return getTables(keyspace);
    }
    
    public List<View> getViews(String keyspace) {
        return cluster.getMetadata().getKeyspace(keyspace).getMaterializedViews().stream()
                .map(t -> new View(t)).sorted(Comparator.comparing(View::getName)).collect(toList());
    }
    
    public List<View> getViews() {
        return getViews(keyspace);
    }
    
    public ResultSet executeQuery(String query) {
        return session.execute(query);
    }
    
    private static Cluster getCluster(ConnectionInfo connectionInfo) {
        Cluster.Builder builder = Cluster.builder()
                .addContactPoints(connectionInfo.getContactPoints().split(","))
                .withoutMetrics()
                .withoutJMXReporting()
                .withQueryOptions(new QueryOptions().setConsistencyLevel(ConsistencyLevel.LOCAL_ONE))
                .withAuthProvider(new PlainTextAuthProvider(
                        connectionInfo.getUsername(), connectionInfo.getPassword()));
                
        if (connectionInfo.getJksFile() != null && !connectionInfo.getJksFile().isEmpty()) {
            File jksFile = new File(connectionInfo.getJksFile());
            if (!jksFile.exists()) {
                throw new RuntimeException("JKS file not found in the specified location");
            }
            
            if (connectionInfo.getJksPassword() != null && !connectionInfo.getJksPassword().isEmpty()) {
                configureSSL(builder, connectionInfo.getJksPassword(), jksFile);
            }
        }
        
        return builder.build();
    }
    
    @SuppressWarnings("deprecation")
    private static void configureSSL(Cluster.Builder builder, String jksPassword, File jksFile) {
        builder.withSSL(RemoteEndpointAwareJdkSSLOptions.builder()
                .withSSLContext(getSSLContext(jksPassword, jksFile)).build());
    }
    
    private static SSLContext getSSLContext(String jksPassword, File jksFile) {
        try {
            KeyStore trustStore = KeyStore.getInstance("JKS");
            
            try (InputStream stream = new BufferedInputStream(new FileInputStream(jksFile))) {
                trustStore.load(stream, jksPassword.toCharArray());
            }
    
            TrustManagerFactory trustManagerFactory = 
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
    
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (KeyStoreException  | CertificateException | NoSuchAlgorithmException | KeyManagementException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
}
