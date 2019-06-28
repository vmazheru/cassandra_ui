package com.ask.cui.ui.components.querytab;

import static java.util.stream.Collectors.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.ask.cui.ui.Labels;
import com.datastax.driver.core.ColumnDefinitions.Definition;
import com.datastax.driver.core.ResultSet;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class ResultSetTableView extends TableView<List<String>> {
    
    public ResultSetTableView(ResultSet rs) {
        if (!rs.isExhausted()) {
            TableViewSelectionModel<List<String>> selectionModel = getSelectionModel();
            
            selectionModel.setCellSelectionEnabled(true);
            selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
            
            MenuItem copyMenuItem = new MenuItem(Labels.COPY);
            copyMenuItem.setOnAction(e -> {
                String clipboardContent = 
                getSelectionModel().getSelectedCells().stream()
                    .map(pos -> Objects.toString(pos.getTableColumn().getCellData(pos.getRow())))
                    .collect(joining(System.lineSeparator()));
                
                ClipboardContent clipboard = new ClipboardContent();
                clipboard.putString(clipboardContent);
                Clipboard.getSystemClipboard().setContent(clipboard);
            });
            ContextMenu ctxMenu = new ContextMenu(copyMenuItem);
            setContextMenu(ctxMenu);
            
            List<Definition> columnDefinitions = rs.getColumnDefinitions().asList();
            
            for (int i = 0; i < columnDefinitions.size(); i++) {
                Definition def = columnDefinitions.get(i);
                TableColumn<List<String>, String> c = new TableColumn<>();
                c.setText(def.getName());
                
                int idx = i;
                c.setCellValueFactory(data -> {
                    List<String> rowValues = data.getValue();
                    String cellValue = rowValues.get(idx);
                    return new ReadOnlyStringWrapper(cellValue);
                });
                
                c.setCellFactory(col -> new DragSelectionCell());
                
                getColumns().add(c);
            }
            
            rs.forEach(row -> {
                List<String> tblRow = new ArrayList<>(columnDefinitions.size());
                
                for (int i = 0; i < columnDefinitions.size(); i++) {
                    Object obj = row.getObject(i);
                    tblRow.add(obj != null ? obj.toString() : "");
                }
                
                getItems().add(tblRow);
            });
        } else {
            this.setPlaceholder(new Label("No result set retuned"));
        }
    }
    
    // TODO: work on table cell selection so it selects rectangular ranges of cells
    public static class DragSelectionCell extends TableCell<List<String>, String> {  
        public DragSelectionCell() {
            setOnDragDetected(e -> {
                startFullDrag();  
                getTableColumn().getTableView().getSelectionModel().select(getIndex(), getTableColumn());
            });
            
            setOnMouseDragEntered(e -> {
                getTableColumn().getTableView().getSelectionModel().select(getIndex(), getTableColumn());
            });
        }  
        
        @Override  
        public void updateItem(String item, boolean empty) {  
            super.updateItem(item, empty);
            if (empty) {  
                setText(null);  
            } else {  
                setText(item);  
            }  
        }  
    }

}
