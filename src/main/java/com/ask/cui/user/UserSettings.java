package com.ask.cui.user;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import com.ask.cui.connector.ConnectionInfo;

public final class UserSettings {
    
    private static final String APPLICATION_DIR = ".cassandraui";
    private static final String CONNECTION_INFO_FILE = "connections.ser";
    
    private UserSettings() {}
    
    public static List<ConnectionInfo> loadUserConnections() {
        Path connectionInfoFile = getConnectionInfoFile();
        if (Files.exists(connectionInfoFile)) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(connectionInfoFile.toFile()))) {
                @SuppressWarnings("unchecked")
                List<ConnectionInfo> infos = (List<ConnectionInfo>)in.readObject();
                return infos;
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Files.delete(connectionInfoFile);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return Collections.emptyList();
    }
    
    public static void storeUserConnections(List<ConnectionInfo> connectionInfos) {
        Path connectionInfoFile = getConnectionInfoFile();
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(connectionInfoFile.toFile()))) {
            out.writeObject(connectionInfos);
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private static Path getConnectionInfoFile() {
        return Paths.get(getApplicationDir().toString(), CONNECTION_INFO_FILE);
    }
    
    private static Path getApplicationDir() {
        try {
            Path applicationDir = Paths.get(getUserDir().toString(), APPLICATION_DIR);
            if (!Files.exists(applicationDir)) {
                Files.createDirectories(applicationDir);
            }
            return applicationDir;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static Path getUserDir() {
        return Paths.get(System.getProperty("user.home"));
    }

}
