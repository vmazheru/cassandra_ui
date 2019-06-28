package com.ask.cui.ui.components.objecttree;

import com.ask.cui.connector.model.AbstractTable;
import com.ask.cui.ui.Constants;
import com.ask.cui.ui.Labels;
import com.ask.cui.ui.main.ApplicationUI;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class DBObjectTreeItem extends NavigationTreeItem {
    
    private final DBObjectType type;
    private final Object object;

    public DBObjectTreeItem(String name, ApplicationUI ui, DBObjectType type, Object object) {
        super(name, ui);
        this.type = type;
        this.setImage(type == DBObjectType.TABLE ? Constants.ICON_TABLE : Constants.ICON_VIEW);
        this.object = object;
    }
    
    public DBObjectType getType() {
        return type;
    }
    
    @Override
    protected ContextMenu getContextMenu() {
        if (type == DBObjectType.TABLE || type == DBObjectType.VIEW) {
            MenuItem describeMenuItem = new MenuItem(Labels.DESCRIBE);
            describeMenuItem.setOnAction(e -> describe());
            return new ContextMenu(describeMenuItem);
        }
        return null;
    }

    private void describe() {
        if (type == DBObjectType.TABLE || type == DBObjectType.VIEW) {
            AbstractTable t = (AbstractTable)object;
            String cql = t.toCreateCql();
            ConnectionTreeItem connectionTreeItem = (ConnectionTreeItem)getParent().getParent();
            getUI().getQueryTabPane().setText(connectionTreeItem.getConnectionInfo(), cql);
        }
    }
}
