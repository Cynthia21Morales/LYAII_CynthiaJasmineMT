package com.cynthiasystems;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApplication extends Application {
    @Override public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cynthiasystems/view/analisissemantico.fxml"));
        stage.setScene(new Scene(loader.load(), 800, 600));
        stage.setTitle("Análisis Semántico");
        stage.show();
    }
    public static void main(String[] args) { launch(args); }
}