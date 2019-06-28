package com.ask.cui.ui.components.objecttree;

import com.ask.cui.ui.main.ApplicationUI;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public abstract class NavigationTreeItem extends TreeItem<Label> {
    
    private final ApplicationUI ui;
    
    protected NavigationTreeItem(String name, ApplicationUI ui) {
        this.ui = ui;
        Label label = new Label(name);
        
        setValue(label);
        label.setOnMousePressed(e -> {
            if (e.isSecondaryButtonDown()) {
                ContextMenu cMenu = getContextMenu();
                if (cMenu != null) {
                    cMenu.show(label, e.getScreenX(), e.getScreenY());
                }
            }
        });
    }
    
    protected ApplicationUI getUI() {
        return ui;
    }
    
    protected void setImage(String path) {
        setGraphic(new ImageView(new Image(path)));
    }

    protected abstract ContextMenu getContextMenu();
    
}
