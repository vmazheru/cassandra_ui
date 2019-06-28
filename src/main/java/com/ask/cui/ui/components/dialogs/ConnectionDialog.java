package com.ask.cui.ui.components.dialogs;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.ask.cui.connector.CassandraConnection;
import com.ask.cui.connector.ConnectionInfo;
import com.ask.cui.connector.ConnectionManager;
import com.ask.cui.ui.Labels;
import com.ask.cui.ui.components.objecttree.ConnectionTreeItem;
import com.ask.cui.ui.components.objecttree.RootTreeItem;
import com.ask.cui.ui.main.ApplicationUI;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ConnectionDialog extends Stage {

    public ConnectionDialog(RootTreeItem treeItem, ApplicationUI ui) {
        initModality(Modality.APPLICATION_MODAL);
        setTitle(Labels.NEW_CONNECTION);
        
        BorderPane root = new BorderPane();
        Scene scene = new Scene(root);
        setScene(scene);
        
        GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        grid.setPadding(new Insets(10, 10, 10, 10));
        
        TextField connectionName = new TextField();
        TextField contactPoints = new TextField();
        TextField username = new TextField();
        PasswordField password = new PasswordField();
        
        TextField jksPassword = new TextField();
        TextField jksFile = new TextField();
        Button jksFileBtn = new Button("Select File");
        
        ComboBox<String> keyspace = new ComboBox<>();
        
        Button createBtn = new Button(Labels.CREATE);
        Button cancelBtn = new Button(Labels.CANCEL);
        Button testBtn = new Button(Labels.CONNECT_AND_LOAD_KEYSPACES);
        
        Label statusBar = new Label();
        statusBar.setAlignment(Pos.BOTTOM_LEFT);
        statusBar.setPadding(new Insets(0, 10, 5, 10));
        
        boolean disableButtons = 
                connectionName.getText().trim().isEmpty() ||
                contactPoints.getText().trim().isEmpty() ||
                username.getText().trim().isEmpty() ||
                password.getText().trim().isEmpty();
        
        createBtn.setDisable(disableButtons);
        testBtn.setDisable(disableButtons);
        
        EventHandler<? super KeyEvent> keyTypedHandler = e -> {
            boolean disable = 
                    connectionName.getText().trim().isEmpty() ||
                    contactPoints.getText().trim().isEmpty() ||
                    username.getText().trim().isEmpty() ||
                    password.getText().trim().isEmpty();

            createBtn.setDisable(disable);
            testBtn.setDisable(disable);
            
            if (!keyspace.getItems().isEmpty()) {
                keyspace.getItems().clear();
            }
        };
        
        connectionName.setOnKeyTyped(keyTypedHandler);
        contactPoints.setOnKeyTyped(keyTypedHandler);
        username.setOnKeyTyped(keyTypedHandler);
        password.setOnKeyTyped(keyTypedHandler);
        
        Runnable connectAndLoadKeyspaces = () -> {
            statusBar.setStyle("-fx-text-fill: black;");
            statusBar.setText(Labels.CONNECTING);
            scene.setCursor(Cursor.WAIT);

            new Thread(() -> {
                ConnectionInfo connectionInfo = new ConnectionInfo(
                        connectionName.getText().trim(),
                        contactPoints.getText().trim(),
                        username.getText().trim(), 
                        password.getText().trim(),
                        jksPassword.getText().trim(),
                        jksFile.getText().trim(),
                        Optional.empty());
                try {
                    CassandraConnection conn = ConnectionManager.connect(connectionInfo);
                    List<String> keyspaces = conn.getKeyspaceNames();
                    
                    Platform.runLater(() -> { 
                        if (keyspace.getItems().isEmpty()) {
                            keyspace.getItems().addAll(keyspaces);
                            keyspace.getSelectionModel().select(0);
                        }                        
                        statusBar.setText(Labels.SUCCESS);
                    });
                } catch (Exception ex) {
                    statusBar.setStyle("-fx-text-fill: red;");
                    Platform.runLater(() -> statusBar.setText(ex.getMessage()));                    
                } finally {
                    ConnectionManager.disconnect(connectionInfo);
                    Platform.runLater(() -> scene.setCursor(Cursor.DEFAULT));
                }
                
            }).start();
        };
        
        jksFileBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open JKS File");
            fileChooser.getExtensionFilters().add(new ExtensionFilter("JKS Files", "*.jks"));
            File selectedFile = fileChooser.showOpenDialog(this);
            jksFile.setText(selectedFile.getAbsolutePath());
        });
        
        createBtn.setOnAction(e -> {
            if (keyspace.getItems().isEmpty()) {
                statusBar.setStyle("-fx-text-fill: black;");
                statusBar.setText(Labels.TEST_AND_LOAD_KEYSPACES);
            } else {
                TreeItem<Label> newTreeItem = new ConnectionTreeItem(new ConnectionInfo(
                        connectionName.getText().trim(),
                        contactPoints.getText().trim(),
                        username.getText().trim(), 
                        password.getText().trim(),
                        jksPassword.getText().trim(),
                        jksFile.getText().trim(),
                        Optional.ofNullable(keyspace.getValue())), ui);
                treeItem.getChildren().add(newTreeItem);
                treeItem.setExpanded(true);
                treeItem.getChildren().sort(Comparator.comparing(ti -> ti.getValue().getText()));
                
                treeItem.saveConnections();
                
                close();
            }
        });
        
        cancelBtn.setOnAction(e -> close());
        testBtn.setOnAction(e -> connectAndLoadKeyspaces.run());

        FlowPane jksFilePane = new FlowPane(Orientation.HORIZONTAL, 10, 0, jksFile, jksFileBtn);
        
        grid.add(new Label(Labels.CONNECTION_NAME), 0, 0); grid.add(connectionName, 1, 0);
        grid.add(new Label(Labels.CONTACT_POINTS), 0, 1); grid.add(contactPoints, 1, 1);
        grid.add(new Label(Labels.USERNAME), 0, 2); grid.add(username, 1, 2);
        grid.add(new Label(Labels.PASSWORD), 0, 3); grid.add(password, 1, 3);
        grid.add(new Label(Labels.JKS_PASSWORD), 0, 4); grid.add(jksPassword, 1, 4);
        grid.add(new Label(Labels.JKS_FILE), 0, 5); grid.add(jksFilePane, 1, 5);  
        grid.add(new Label(Labels.KEYSPACE), 0, 6); grid.add(keyspace, 1, 6);
        
        BorderPane buttonPane = new BorderPane();
        buttonPane.setLeft(testBtn);
        FlowPane fp = new FlowPane(Orientation.HORIZONTAL, 10, 0, createBtn, cancelBtn);
        fp.setAlignment(Pos.CENTER_RIGHT);
        buttonPane.setRight(fp);
        buttonPane.setPadding(new Insets(10, 0, 0, 0));
        grid.add(buttonPane, 1, 7);
        
        root.setCenter(grid);
        root.setBottom(statusBar);
        
        sizeToScene();
        setResizable(false);
        show();
        centerOnScreen();
    }
    
}
