package com.cynthiasystems.generaciondecodigointermedio.registros;

import com.cynthiasystems.generaciondecodigointermedio.CompilerContext;
import com.cynthiasystems.generaciondecodigointermedio.Navegador;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class RegistrosController {

    @FXML private Label etiquetaExpresion;
    @FXML private TextArea salida;

    @FXML
    private void initialize() {
        etiquetaExpresion.setText("Expresión fuente: " + CompilerContext.expresion);

        StringBuilder sb = new StringBuilder();
        sb.append("Expresión postfija: ").append(String.join(" ", CompilerContext.postfija)).append("\n\n");
        sb.append("Código generado con asignación de registros:\n");
        for (String instr : CompilerContext.instruccionesRegistros) {
            sb.append("  ").append(instr).append("\n");
        }
        sb.append("\nRegistros distintos utilizados: ").append(CompilerContext.registrosUsados);
        salida.setText(sb.toString());
    }

    @FXML
    private void siguiente() {
        Navegador.cargar("ensamblador/ensamblador-view.fxml");
    }

    @FXML
    private void volverInicio() {
        Navegador.cargar("entrada-view.fxml");
    }
}
