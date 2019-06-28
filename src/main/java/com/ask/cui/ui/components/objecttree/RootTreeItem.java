package com.ask.cui.ui.components.objecttree;

import static java.util.stream.Collectors.*;

import java.util.List;

import com.ask.cui.connector.ConnectionInfo;
import com.ask.cui.connector.ConnectionManager;
import com.ask.cui.ui.Labels;
import com.ask.cui.ui.components.dialogs.ConnectionDialog;
import com.ask.cui.ui.main.ApplicationUI;
import com.ask.cui.user.UserSettings;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class RootTreeItem extends NavigationTreeItem {
    
    public RootTreeItem(ApplicationUI ui) {
        super(Labels.CONNECTIONS, ui);
        setExpanded(true);
        UserSettings.loadUserConnections().stream().map(ci -> new ConnectionTreeItem(ci, getUI()))
            .forEach(connectionTreeItem -> getChildren().add(connectionTreeItem));
    }
    
    public void saveConnections() {
        List<ConnectionInfo> connectionInfos = getChildren().stream()
                .map(ti -> ((ConnectionTreeItem)ti).getConnectionInfo()).collect(toList());
        UserSettings.storeUserConnections(connectionInfos);
    }
    
    public void deleteConnection(ConnectionTreeItem connectionTreeItem) {
        ConnectionManager.disconnect(connectionTreeItem.getConnectionInfo());
        getUI().getQueryTabPane().closeTab(connectionTreeItem.getConnectionInfo());
        getChildren().remove(connectionTreeItem);
        saveConnections();
    }
    
    @Override
    protected ContextMenu getContextMenu() {
        MenuItem newConnectionMenuItem = new MenuItem(Labels.NEW_CONNECTION);
        newConnectionMenuItem.setOnAction(e -> new ConnectionDialog(this, getUI()));
        return new ContextMenu(newConnectionMenuItem);
    }

}
