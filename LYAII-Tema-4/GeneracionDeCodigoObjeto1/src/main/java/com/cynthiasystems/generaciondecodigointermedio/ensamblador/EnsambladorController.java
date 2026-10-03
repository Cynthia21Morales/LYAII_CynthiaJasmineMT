package com.cynthiasystems.generaciondecodigointermedio.ensamblador;

import com.cynthiasystems.generaciondecodigointermedio.CompilerContext;
import com.cynthiasystems.generaciondecodigointermedio.Navegador;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.ArrayList;
import java.util.List;

public class EnsambladorController {

    /** Fila mostrada en la tabla (dirección, etiqueta, instrucción fuente y código traducido). */
    public static class Fila {
        private final SimpleIntegerProperty direccion;
        private final SimpleStringProperty etiqueta;
        private final SimpleStringProperty fuente;
        private final SimpleStringProperty codigo;

        Fila(int dir, String etiqueta, String fuente, String codigo) {
            this.direccion = new SimpleIntegerProperty(dir);
            this.etiqueta = new SimpleStringProperty(etiqueta == null ? "" : etiqueta);
            this.fuente = new SimpleStringProperty(fuente);
            this.codigo = new SimpleStringProperty(codigo);
        }

        public int getDireccion() { return direccion.get(); }
        public String getEtiqueta() { return etiqueta.get(); }
        public String getFuente() { return fuente.get(); }
        public String getCodigo() { return codigo.get(); }
    }

    @FXML private TextArea entrada;
    @FXML private TableView<Fila> tabla;
    @FXML private TableColumn<Fila, Number> colDireccion;
    @FXML private TableColumn<Fila, String> colEtiqueta;
    @FXML private TableColumn<Fila, String> colFuente;
    @FXML private TableColumn<Fila, String> colCodigo;

    private final EnsambladorSimulador simulador = new EnsambladorSimulador();

    @FXML
    private void initialize() {
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colEtiqueta.setCellValueFactory(new PropertyValueFactory<>("etiqueta"));
        colFuente.setCellValueFactory(new PropertyValueFactory<>("fuente"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        // El programa ensamblador ya viene generado desde la fase de registros.
        entrada.setText(CompilerContext.programaEnsamblador);
        cargarTabla(CompilerContext.lineasEnsambladas);
    }

    @FXML
    private void ensamblar() {
        CompilerContext.lineasEnsambladas = simulador.ensamblar(entrada.getText());
        cargarTabla(CompilerContext.lineasEnsambladas);
    }

    private void cargarTabla(List<EnsambladorSimulador.LineaEnsamblada> lineas) {
        List<Fila> filas = new ArrayList<>();
        for (EnsambladorSimulador.LineaEnsamblada l : lineas) {
            filas.add(new Fila(l.direccion, l.etiqueta, l.fuente, l.codigoMaquina));
        }
        tabla.getItems().setAll(filas);
    }

    @FXML
    private void siguiente() {
        Navegador.cargar("lenguajemaquina/lenguajemaquina-view.fxml");
    }

    @FXML
    private void volverInicio() {
        Navegador.cargar("entrada-view.fxml");
    }
}
