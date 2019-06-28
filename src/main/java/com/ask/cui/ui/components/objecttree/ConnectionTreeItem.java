package com.ask.cui.ui.components.objecttree;

import com.ask.cui.connector.CassandraConnection;
import com.ask.cui.connector.ConnectionInfo;
import com.ask.cui.connector.ConnectionManager;
import com.ask.cui.connector.model.Column;
import com.ask.cui.connector.model.Table;
import com.ask.cui.connector.model.View;
import com.ask.cui.ui.Constants;
import com.ask.cui.ui.Labels;
import com.ask.cui.ui.main.ApplicationUI;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ConnectionTreeItem extends NavigationTreeItem {
    
    private final ConnectionInfo connectionInfo;

    public ConnectionTreeItem(ConnectionInfo connectionInfo, ApplicationUI ui) {
        super(connectionInfo.getName(), ui);
        this.connectionInfo = connectionInfo;
        setImage(Constants.ICON_DATABASE);
    }
    
    @Override
    public ContextMenu getContextMenu() {
        MenuItem connectOrDisconnectMenuItem = isConnected() ? 
                new MenuItem(Labels.DISCONNECT) : new MenuItem(Labels.CONNECT);
        connectOrDisconnectMenuItem.setOnAction(isConnected() ? e -> disconnect() : e -> connect());
        
        MenuItem deleteMenuItem = new MenuItem(Labels.DELETE);
        deleteMenuItem.setOnAction(e -> ((RootTreeItem)getParent()).deleteConnection(this));
        
        return new ContextMenu(connectOrDisconnectMenuItem, deleteMenuItem);
    }
    
    public ConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }
    
    public boolean isConnected() {
        return ConnectionManager.isConnected(connectionInfo);
    }
    
    private void connect() {
        if (!isConnected()) {
            CassandraConnection connection = ConnectionManager.connect(connectionInfo);
            
            ApplicationUI ui = getUI();
            
            DBObjectGroupTreeItem tables = new DBObjectGroupTreeItem(DBObjectType.TABLE, ui);
            DBObjectGroupTreeItem views = new DBObjectGroupTreeItem(DBObjectType.VIEW, ui);
            
            tables.setExpanded(true);
            views.setExpanded(true);
            
            for (Table t : connection.getTables()) {
                DBObjectTreeItem tableTreeItem = new DBObjectTreeItem(t.getName(), ui, DBObjectType.TABLE, t);
                for (Column c : t.getColumns()) {
                    tableTreeItem.getChildren().add(new TreeItem<>(getColumnLabel(c))); 
                }
                tables.getChildren().add(tableTreeItem);
            }
            
            for (View v : connection.getViews()) {
                DBObjectTreeItem viewTreeItem = new DBObjectTreeItem(v.getName(), ui, DBObjectType.VIEW, v);
                for (Column c : v.getColumns()) {
                    viewTreeItem.getChildren().add(new TreeItem<>(getColumnLabel(c))); 
                }
                views.getChildren().add(viewTreeItem);
            }
            
            getChildren().add(tables);
            getChildren().add(views);
            setExpanded(true);
            
            changeConnectionStatus();
            
            getUI().getQueryTabPane().showTab(connectionInfo);
        }
    }
    
    private void disconnect() {
        if (isConnected()) {
            ConnectionManager.disconnect(connectionInfo);
            getChildren().clear();
            changeConnectionStatus();
            getUI().getQueryTabPane().closeTab(connectionInfo);
        }
    }
    
    private void changeConnectionStatus() {
        if (isConnected()) {
            setImage(Constants.ICON_DATABASE_CONNECTED);
        } else {
            setImage(Constants.ICON_DATABASE);
        }
    }
    
    private static Label getColumnLabel(Column c) {
        StringBuilder sb = new StringBuilder();
        sb.append(c.getName()).append(" ").append(c.getType().toUpperCase());
        if (c.isPartitionKey()) sb.append(" (P)");
        else if (c.isClusteringKey()) sb.append(" (C)");
        
        Label l = new Label(sb.toString(), new ImageView(new Image(Constants.ICON_COLUMN)));
        if (c.isPartitionKey() || c.isClusteringKey()) l.setStyle("-fx-font-weight: bold");
        return l;
    }
    
}
