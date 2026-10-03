package com.cynthiasystems.generaciondecodigointermedio;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class EntradaController {

    @FXML private TextField campoExpresion;
    @FXML private Label etiquetaError;

    @FXML
    private void compilar() {
        String expresion = campoExpresion.getText();
        try {
            CompilerContext.compilar(expresion);
            Navegador.cargar("registros/registros-view.fxml");
        } catch (Exception ex) {
            etiquetaError.setText("Expresión no válida. Usa variables, + - * / y paréntesis.");
        }
    }
}
