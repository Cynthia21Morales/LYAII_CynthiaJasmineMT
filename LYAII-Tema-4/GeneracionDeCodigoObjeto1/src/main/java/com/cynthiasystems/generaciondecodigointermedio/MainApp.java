package com.cynthiasystems.generaciondecodigointermedio;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("compilador-view.fxml"));
        Parent raiz = loader.load();

        primaryStage.setTitle("Compilador: Generación de código objeto");
        primaryStage.setScene(new Scene(raiz, 600, 400));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
