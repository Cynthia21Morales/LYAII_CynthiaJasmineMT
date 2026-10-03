package com.cynthiasystems.generaciondecodigointermedio.memoria;

import com.cynthiasystems.generaciondecodigointermedio.CompilerContext;
import com.cynthiasystems.generaciondecodigointermedio.Navegador;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.HashMap;
import java.util.Map;

public class MemoriaController {

    @FXML private FlowPane cuadricula;
    @FXML private TextField campoVariable;
    @FXML private TextField campoTamanio;
    @FXML private Label etiquetaEstado;

    private final MemoriaSimulada memoria = new MemoriaSimulada();
    private final Map<String, Color> coloresPorVariable = new HashMap<>();
    private final Color[] paleta = {
            Color.web("#4C9AFF"), Color.web("#57D9A3"), Color.web("#FF8F73"),
            Color.web("#B197FC"), Color.web("#FFC400"), Color.web("#FF6B81")
    };
    private int siguienteColor = 0;

    @FXML
    private void initialize() {
        for (String variable : CompilerContext.variables) {
            try {
                memoria.asignar(variable, 1);
                coloresPorVariable.put(variable, paleta[siguienteColor++ % paleta.length]);
            } catch (Exception ignored) {
                // memoria llena o variable repetida: se omite en esta demo
            }
        }
        dibujarMemoria();
        actualizarEstado();
    }

    @FXML
    private void asignar() {
        try {
            String nombre = campoVariable.getText().trim();
            int tamanio = Integer.parseInt(campoTamanio.getText().trim());
            if (nombre.isEmpty() || tamanio <= 0) throw new IllegalArgumentException("Datos inválidos.");
            memoria.asignar(nombre, tamanio);
            coloresPorVariable.put(nombre, paleta[siguienteColor++ % paleta.length]);
            dibujarMemoria();
            actualizarEstado();
        } catch (Exception ex) {
            etiquetaEstado.setText("Error: " + ex.getMessage());
        }
    }

    @FXML
    private void liberar() {
        String nombre = campoVariable.getText().trim();
        memoria.liberar(nombre);
        coloresPorVariable.remove(nombre);
        dibujarMemoria();
        actualizarEstado();
    }

    @FXML
    private void recolectarBasura() {

        String vivasTexto = campoVariable.getText().trim();
        java.util.List<String> vivas = vivasTexto.isEmpty()
                ? java.util.List.of()
                : java.util.Arrays.asList(vivasTexto.split("\\s*,\\s*"));
        memoria.recolectarBasura(vivas);
        coloresPorVariable.keySet().retainAll(memoria.getTablaAsignacion().keySet());
        dibujarMemoria();
        actualizarEstado();
    }

    @FXML
    private void finalizar() {
        Navegador.cargar("entrada-view.fxml");
    }

    private void dibujarMemoria() {
        cuadricula.getChildren().clear();
        String[] celdas = memoria.getCeldas();
        for (int i = 0; i < celdas.length; i++) {
            Rectangle celda = new Rectangle(28, 28);
            String duenio = celdas[i];
            celda.setFill(duenio == null ? Color.web("#E0E0E0") : coloresPorVariable.getOrDefault(duenio, Color.GRAY));
            celda.setStroke(Color.web("#999"));
            Tooltip.install(celda, new Tooltip("Dirección " + i + (duenio != null ? " -> " + duenio : " (libre)")));
            cuadricula.getChildren().add(celda);
        }
    }

    private void actualizarEstado() {
        etiquetaEstado.setText("Localidades libres: " + memoria.espacioLibreTotal() + " / " + MemoriaSimulada.TAMANIO
                + "   |   Variables en memoria: " + memoria.getTablaAsignacion().keySet());
    }
}
