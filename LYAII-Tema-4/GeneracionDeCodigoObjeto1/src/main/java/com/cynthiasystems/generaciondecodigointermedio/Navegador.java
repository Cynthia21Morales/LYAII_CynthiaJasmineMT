package com.cynthiasystems.generaciondecodigointermedio;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;

import java.io.IOException;


public class Navegador {

    private static Stage stage;

    public static void setStage(Stage s) {
        stage = s;
    }

    public static void cargar(String rutaFxml) {
        try {
            FXMLLoader loader = new FXMLLoader(Navegador.class.getResource(rutaFxml));
            Parent root = loader.load();
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo cargar la vista: " + rutaFxml, e);
        }
    }
}
