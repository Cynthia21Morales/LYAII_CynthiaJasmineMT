package com.cynthiasystems.generaciondecodigointermedio.lenguajemaquina;

import com.cynthiasystems.generaciondecodigointermedio.CompilerContext;
import com.cynthiasystems.generaciondecodigointermedio.Navegador;
import com.cynthiasystems.generaciondecodigointermedio.ensamblador.EnsambladorSimulador;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class LenguajeMaquinaController {

    @FXML private TextArea programaBinario;
    @FXML private TextField campoDecimal;
    @FXML private Label resultadoConversion;
    @FXML private TextField campoBinario;
    @FXML private TextArea resultadoInstruccion;

    private final ConversorBinario conversor = new ConversorBinario();

    @FXML
    private void initialize() {
        StringBuilder sb = new StringBuilder();
        for (EnsambladorSimulador.LineaEnsamblada linea : CompilerContext.lineasEnsambladas) {
            String hex = linea.codigoMaquina;
            String binario = hex.startsWith("ERROR") ? "—" : conversor.hexABinario(hex);
            sb.append(String.format("Dir %02d  hex=%-6s  binario=%s%n", linea.direccion, hex, binario));
        }
        programaBinario.setText(sb.toString());
    }

    @FXML
    private void convertir() {
        try {
            long valor = Long.parseLong(campoDecimal.getText().trim());
            resultadoConversion.setText(
                    "Decimal: " + valor + "\n" +
                    "Binario: " + conversor.decimalABinario(valor) + "\n" +
                    "Hexadecimal: " + conversor.decimalAHex(valor));
        } catch (NumberFormatException ex) {
            resultadoConversion.setText("Introduce un número entero válido.");
        }
    }

    @FXML
    private void interpretar() {
        resultadoInstruccion.setText(conversor.interpretarInstruccion(campoBinario.getText()));
    }

    @FXML
    private void siguiente() {
        Navegador.cargar("memoria/memoria-view.fxml");
    }

    @FXML
    private void volverInicio() {
        Navegador.cargar("entrada-view.fxml");
    }
}
