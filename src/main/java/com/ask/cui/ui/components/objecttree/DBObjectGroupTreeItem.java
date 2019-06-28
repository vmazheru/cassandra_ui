package com.ask.cui.ui.components.objecttree;

import com.ask.cui.ui.Constants;
import com.ask.cui.ui.main.ApplicationUI;

import javafx.scene.control.ContextMenu;

public class DBObjectGroupTreeItem extends NavigationTreeItem {

    private final DBObjectType type;

    public DBObjectGroupTreeItem(DBObjectType type, ApplicationUI ui) {
        super(type.getDisplayName(), ui);
        this.type = type;
        this.setImage(type == DBObjectType.TABLE ? Constants.ICON_TABLES : Constants.ICON_VIEWS);
    }
    
    public DBObjectType getType() {
        return type;
    }
    
    @Override
    protected ContextMenu getContextMenu() {
        return null;
    }
    
}
