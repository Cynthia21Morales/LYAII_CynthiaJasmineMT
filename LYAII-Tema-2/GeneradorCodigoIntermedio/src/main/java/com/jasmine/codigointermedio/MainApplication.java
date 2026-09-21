package com.jasmine.codigointermedio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {
    public void start(Stage stage) throws Exception {
        FXMLLoader f = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        stage.setScene(new Scene(f.load(), 600, 600));
        stage.setTitle("Generador de Código Intermedio");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}