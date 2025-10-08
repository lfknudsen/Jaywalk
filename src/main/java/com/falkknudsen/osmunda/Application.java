package com.falkknudsen.osmunda;

import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Application extends javafx.application.Application {
    @Override
    public void start(Stage stage) {
        AnchorPane root = new AnchorPane();
        Scene scene = new Scene(root, 320, 240);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
}
