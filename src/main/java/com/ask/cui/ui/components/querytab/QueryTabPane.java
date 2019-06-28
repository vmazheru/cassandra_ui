package com.ask.cui.ui.components.querytab;

import com.ask.cui.connector.ConnectionInfo;
import com.ask.cui.ui.main.ApplicationUI;

import javafx.scene.control.TabPane;

public class QueryTabPane extends TabPane {
    
    private final ApplicationUI ui;
    
    public QueryTabPane(ApplicationUI ui) {
        this.ui = ui;
    }
    
    public void showTab(ConnectionInfo connectionInfo) {
        getSelectionModel().select(getTab(connectionInfo));
    }
    
    public void closeTab(ConnectionInfo connectionInfo) {
        getTabs().stream()
            .filter(t -> t.getText().equals(connectionInfo.getName()))
            .findFirst()
            .ifPresent(tab -> getTabs().remove(tab));
    }
    
    public void setText(ConnectionInfo connectionInfo, String text) {
        getTab(connectionInfo).prependText(text);
    }
    
    private QueryTab getTab(ConnectionInfo connectionInfo) {
        return getTabs().stream()
                .filter(t -> t.getText().equals(connectionInfo.getName()))
                .map(t -> (QueryTab)t)
                .findFirst()
                .orElseGet(() -> {
                    QueryTab newTab = new QueryTab(connectionInfo, ui);
                    getTabs().add(newTab);
                    return newTab;
                });
    }
}
