package com.ask.cui.ui.components.querytab;

import java.util.Arrays;

import com.ask.cui.connector.CassandraConnection;
import com.ask.cui.connector.ConnectionInfo;
import com.ask.cui.connector.ConnectionManager;
import com.ask.cui.ui.Labels;
import com.ask.cui.ui.main.ApplicationUI;

import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;

public class QueryTab extends Tab {
    
    private static int DEFAULT_TABLE_SIZE_LIMIT = 100;
    
    private final CassandraConnection connection;
    private final TextArea queryArea;
    
    public QueryTab(ConnectionInfo connectionInfo, ApplicationUI ui) {
        super(connectionInfo.getName());
        
        connection = ConnectionManager.connect(connectionInfo);
        
        SplitPane splitPane = new SplitPane();
        setContent(splitPane);
        
        splitPane.setOrientation(Orientation.VERTICAL);
        
        queryArea = new TextArea("");
        queryArea.setPromptText(Labels.EXECUTE_STATEMENT_HINT);
        
        queryArea.setOnKeyReleased(e -> {
            if (e.getCode() == KeyCode.ENTER && e.isControlDown()) {
                if (connection != null) {
                    String statement = getStatement(queryArea);
                    if (statement != null) {
                        try {
                            String cql = addLimitToSelectIfAbsent(statement);
                            ui.setStatus(Labels.EXECUTING + cql);
                            ResultSetTableView table = new ResultSetTableView(connection.executeQuery(cql));
                            if (splitPane.getItems().size() == 2) {
                                splitPane.getItems().remove(1);
                            }
                            splitPane.getItems().add(table);
                        } catch (Exception ex) {
                            if (splitPane.getItems().size() == 2) {
                                splitPane.getItems().remove(1);
                            }
                            splitPane.getItems().add(new TextArea(ex.getMessage()));
                        }
                    } else {
                        ui.setStatus("No statement to execute");
                    }
                } else {
                    ui.setStatus("Disconnected from keyspace");
                    if (splitPane.getItems().size() == 2) {
                        splitPane.getItems().remove(1);
                    }
                }
            }
        });
        
        splitPane.getItems().add(queryArea);
    }
    
    public void prependText(String text) {
        String newLine = System.lineSeparator();
        queryArea.setText(text + newLine + newLine + queryArea.getText());
    }
    
    private static String getStatement(TextArea area) {
        String selectedText = area.getSelectedText();
        if (selectedText != null && !selectedText.trim().isEmpty()) {
            return selectedText;
        }
        
        String text = area.getText().trim();
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        String[] statements = text.split(";");
        
        if (statements.length == 1) {
            return trimToNull(statements[0]);
        }
        
        int caretPosition = area.getCaretPosition();
        
        if (caretPosition >= text.length()) {
            return trimToNull(statements[statements.length - 1]);
        }
        
        int totalLength = 0;
        for (String statement : statements) {
            totalLength += (statement.length() + 1);
            if (totalLength > caretPosition) {
                return trimToNull(statement);
            }
        }
        
        return null;
    }
    
    private static String trimToNull(String s) {
        if (s == null) return s;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    
    // TODO: move to CQL utils
    private static String addLimitToSelectIfAbsent(String cql) {
        String actualCql = cql.trim();
        actualCql = actualCql.endsWith(";") ? actualCql.substring(0, actualCql.length()-1) : actualCql;
        
        String[] tokens = actualCql.split("\\s+");
        if ("SELECT".equalsIgnoreCase(tokens[0]) && tokens.length > 2) {
            boolean isAllowFiltering = "ALLOW".equalsIgnoreCase(tokens[tokens.length - 2]) && 
                                       "FILTERING".equalsIgnoreCase(tokens[tokens.length - 1]);
            
            boolean hasLimit = isAllowFiltering ? 
                    "LIMIT".equalsIgnoreCase(tokens[tokens.length - 4]) : 
                    "LIMIT".equalsIgnoreCase(tokens[tokens.length - 2]); 
            
            if (hasLimit) {
                return actualCql;
            }
            
            if (isAllowFiltering) {
                return String.join(" ", Arrays.asList(tokens).subList(0, tokens.length - 2)) + 
                    " LIMIT " + DEFAULT_TABLE_SIZE_LIMIT + 
                    " ALLOW FILTERING";
            }
            
            return actualCql + " LIMIT " + DEFAULT_TABLE_SIZE_LIMIT;
        }
        
        return actualCql;
    }

}
