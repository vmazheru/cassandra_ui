package com.ask.cui.ui.main;

import com.ask.cui.ui.components.objecttree.RootTreeItem;
import com.ask.cui.ui.components.querytab.QueryTabPane;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;

public class ApplicationUI extends BorderPane {
    
    private final Label status;
    private final QueryTabPane tabPane;

    public ApplicationUI() {
        status = new Label("");
        status.setAlignment(Pos.BOTTOM_LEFT);
        status.setPadding(new Insets(5, 10, 5, 10));
        
        tabPane = new QueryTabPane(this);
        
        SplitPane center = new SplitPane();
        center.getItems().add(new TreeView<>(new RootTreeItem(this)));
        center.getItems().add(tabPane);
        center.setDividerPosition(0, 0.2);
        
        setCenter(center);
        setBottom(status);
    }
    
    public void setStatus(String text) {
        status.setText(text);
    }
    
    public QueryTabPane getQueryTabPane() {
        return tabPane;
    }
    
}
