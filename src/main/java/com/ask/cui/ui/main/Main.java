package com.ask.cui.ui.main;

import com.ask.cui.connector.ConnectionManager;
import com.ask.cui.ui.Labels;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    public static void main(String[] args) {
        launch();
    }
    
    @Override
    public void init() {}
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle(Labels.MAIN_TITLE);
        primaryStage.setScene(new Scene(new ApplicationUI(), 1024, 800));
        primaryStage.show();
    }
    
    @Override
    public void stop() {
        ConnectionManager.disconnectAll();
    }
    
    
    
}
