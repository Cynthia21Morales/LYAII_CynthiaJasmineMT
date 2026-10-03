package com.cynthiasystems.generaciondecodigointermedio;

import com.cynthiasystems.generaciondecodigointermedio.ensamblador.EnsambladorSimulador;
import com.cynthiasystems.generaciondecodigointermedio.lenguajemaquina.ConversorBinario;
import com.cynthiasystems.generaciondecodigointermedio.memoria.MemoriaSimulada;
import com.cynthiasystems.generaciondecodigointermedio.registros.RegistroAllocator;
import com.cynthiasystems.generaciondecodigointermedio.semantico.AnalizadorSemantico;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompiladorController {

    // ---------- Paneles (uno visible a la vez) ----------
    @FXML private Pane panelEntrada;
    @FXML private Pane panelSemantico;
    @FXML private Pane panelRegistros;
    @FXML private Pane panelEnsamblador;
    @FXML private Pane panelLenguajeMaquina;
    @FXML private Pane panelMemoria;

    // ---------- 0. Entrada ----------
    @FXML private TextField campoExpresion;
    @FXML private Label etiquetaErrorEntrada;

    // ---------- 1. Análisis semántico ----------
    @FXML private TableView<FilaSimbolo> tablaSimbolos;
    @FXML private TableColumn<FilaSimbolo, Number> colOrden;
    @FXML private TableColumn<FilaSimbolo, String> colNombre;
    @FXML private TableColumn<FilaSimbolo, String> colTipo;
    @FXML private TextArea erroresSemanticos;
    @FXML private javafx.scene.control.Button botonSiguienteARegistros;

    // ---------- 2. Registros ----------
    @FXML private Label etiquetaExpresionRegistros;
    @FXML private TextArea salidaRegistros;

    // ---------- 3. Ensamblador ----------
    @FXML private TextArea entradaEnsamblador;
    @FXML private TableView<FilaEnsamblada> tablaEnsamblador;
    @FXML private TableColumn<FilaEnsamblada, Number> colDireccion;
    @FXML private TableColumn<FilaEnsamblada, String> colEtiqueta;
    @FXML private TableColumn<FilaEnsamblada, String> colFuente;
    @FXML private TableColumn<FilaEnsamblada, String> colCodigo;

    // ---------- 4. Lenguaje máquina ----------
    @FXML private TextArea programaBinario;
    @FXML private TextField campoDecimal;
    @FXML private Label resultadoConversion;
    @FXML private TextField campoBinario;
    @FXML private TextArea resultadoInstruccion;

    // ---------- 5. Memoria ----------
    @FXML private FlowPane cuadriculaMemoria;
    @FXML private TextField campoVariableMemoria;
    @FXML private TextField campoTamanioMemoria;
    @FXML private Label etiquetaEstadoMemoria;

    // ---------- Lógica de cada fase ----------
    private final RegistroAllocator allocator = new RegistroAllocator();
    private final AnalizadorSemantico analizadorSemantico = new AnalizadorSemantico();
    private final EnsambladorSimulador simuladorEnsamblador = new EnsambladorSimulador();
    private final ConversorBinario conversorBinario = new ConversorBinario();
    private final MemoriaSimulada memoria = new MemoriaSimulada();

    // ---------- Estado que viaja de una fase a otra ----------
    private String expresionFuente;
    private List<String> variables = new ArrayList<>();
    private RegistroAllocator.Resultado resultadoRegistros;
    private String programaEnsambladorGenerado = "";
    private List<EnsambladorSimulador.LineaEnsamblada> lineasEnsambladas = new ArrayList<>();

    private final Map<String, Color> coloresPorVariable = new HashMap<>();
    private final Color[] paleta = {
            Color.web("#4C9AFF"), Color.web("#57D9A3"), Color.web("#FF8F73"),
            Color.web("#B197FC"), Color.web("#FFC400"), Color.web("#FF6B81")
    };
    private int siguienteColor = 0;

    // ==================== Modelos para las tablas ====================

    public static class FilaSimbolo {
        private final SimpleIntegerProperty orden;
        private final SimpleStringProperty nombre;
        private final SimpleStringProperty tipo;

        FilaSimbolo(int orden, String nombre, String tipo) {
            this.orden = new SimpleIntegerProperty(orden);
            this.nombre = new SimpleStringProperty(nombre);
            this.tipo = new SimpleStringProperty(tipo);
        }

        public int getOrden() { return orden.get(); }
        public String getNombre() { return nombre.get(); }
        public String getTipo() { return tipo.get(); }
    }

    public static class FilaEnsamblada {
        private final SimpleIntegerProperty direccion;
        private final SimpleStringProperty etiqueta;
        private final SimpleStringProperty fuente;
        private final SimpleStringProperty codigo;

        FilaEnsamblada(int dir, String etiqueta, String fuente, String codigo) {
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

    // ==================== Inicialización ====================

    @FXML
    private void initialize() {
        colOrden.setCellValueFactory(new PropertyValueFactory<>("orden"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colEtiqueta.setCellValueFactory(new PropertyValueFactory<>("etiqueta"));
        colFuente.setCellValueFactory(new PropertyValueFactory<>("fuente"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));

        mostrarSolo(panelEntrada);
    }

    private void mostrarSolo(Pane panelVisible) {
        for (Pane p : new Pane[]{panelEntrada, panelSemantico, panelRegistros,
                panelEnsamblador, panelLenguajeMaquina, panelMemoria}) {
            boolean esEste = (p == panelVisible);
            p.setVisible(esEste);
            p.setManaged(esEste);
        }
    }

    // ==================== 0 -> 1: Entrada -> Análisis semántico ====================

    @FXML
    private void compilar() {
        expresionFuente = campoExpresion.getText();
        etiquetaErrorEntrada.setText("");
        try {
            resultadoRegistros = allocator.generarCodigo(expresionFuente);
            variables = allocator.extraerVariables(expresionFuente);
        } catch (Exception ex) {
            etiquetaErrorEntrada.setText("Expresión no válida. Usa variables, + - * / y paréntesis.");
            return;
        }

        AnalizadorSemantico.ResultadoSemantico resultadoSemantico =
                analizadorSemantico.analizar(expresionFuente, resultadoRegistros.postfija, variables);

        List<FilaSimbolo> filas = new ArrayList<>();
        for (AnalizadorSemantico.Simbolo s : resultadoSemantico.tablaSimbolos) {
            filas.add(new FilaSimbolo(s.orden, s.nombre, s.tipo));
        }
        tablaSimbolos.getItems().setAll(filas);

        if (resultadoSemantico.valido) {
            erroresSemanticos.setText("Sin errores semánticos. El compilador puede continuar a la generación de código.");
        } else {
            erroresSemanticos.setText(String.join("\n", resultadoSemantico.errores));
        }
        botonSiguienteARegistros.setDisable(!resultadoSemantico.valido);

        mostrarSolo(panelSemantico);
    }

    @FXML
    private void volverEntradaDesdeSemantico() {
        mostrarSolo(panelEntrada);
    }

    // ==================== 1 -> 2: Semántico -> Registros ====================

    @FXML
    private void irARegistros() {
        etiquetaExpresionRegistros.setText("Expresión fuente: " + expresionFuente);

        StringBuilder sb = new StringBuilder();
        sb.append("Expresión postfija: ").append(String.join(" ", resultadoRegistros.postfija)).append("\n\n");
        sb.append("Código generado con asignación de registros:\n");
        for (String instr : resultadoRegistros.instrucciones) {
            sb.append("  ").append(instr).append("\n");
        }
        sb.append("\nRegistros distintos utilizados: ").append(resultadoRegistros.registrosUsados);
        salidaRegistros.setText(sb.toString());

        mostrarSolo(panelRegistros);
    }

    // ==================== 2 -> 3: Registros -> Ensamblador ====================

    @FXML
    private void irAEnsamblador() {
        programaEnsambladorGenerado = traducirAEnsamblador(resultadoRegistros.instrucciones);
        entradaEnsamblador.setText(programaEnsambladorGenerado);
        reensamblar();
        mostrarSolo(panelEnsamblador);
    }

    private String traducirAEnsamblador(List<String> instrucciones) {
        StringBuilder sb = new StringBuilder();
        for (String instr : instrucciones) {
            if (instr.startsWith("¡Sin registros")) continue;
            sb.append(instr.split("->")[0].trim()).append("\n");
        }
        sb.append("HALT");
        return sb.toString();
    }

    @FXML
    private void reensamblar() {
        lineasEnsambladas = simuladorEnsamblador.ensamblar(entradaEnsamblador.getText());
        List<FilaEnsamblada> filas = new ArrayList<>();
        for (EnsambladorSimulador.LineaEnsamblada l : lineasEnsambladas) {
            filas.add(new FilaEnsamblada(l.direccion, l.etiqueta, l.fuente, l.codigoMaquina));
        }
        tablaEnsamblador.getItems().setAll(filas);
    }

    // ==================== 3 -> 4: Ensamblador -> Lenguaje máquina ====================

    @FXML
    private void irALenguajeMaquina() {
        StringBuilder sb = new StringBuilder();
        for (EnsambladorSimulador.LineaEnsamblada linea : lineasEnsambladas) {
            String hex = linea.codigoMaquina;
            String binario = hex.startsWith("ERROR") ? "—" : conversorBinario.hexABinario(hex);
            sb.append(String.format("Dir %02d  hex=%-6s  binario=%s%n", linea.direccion, hex, binario));
        }
        programaBinario.setText(sb.toString());
        mostrarSolo(panelLenguajeMaquina);
    }

    @FXML
    private void convertir() {
        try {
            long valor = Long.parseLong(campoDecimal.getText().trim());
            resultadoConversion.setText(
                    "Decimal: " + valor + "\n" +
                    "Binario: " + conversorBinario.decimalABinario(valor) + "\n" +
                    "Hexadecimal: " + conversorBinario.decimalAHex(valor));
        } catch (NumberFormatException ex) {
            resultadoConversion.setText("Introduce un número entero válido.");
        }
    }

    @FXML
    private void interpretar() {
        resultadoInstruccion.setText(conversorBinario.interpretarInstruccion(campoBinario.getText()));
    }

    // ==================== 4 -> 5: Lenguaje máquina -> Memoria ====================

    @FXML
    private void irAMemoria() {
        for (String variable : variables) {
            try {
                memoria.asignar(variable, 1);
                coloresPorVariable.put(variable, paleta[siguienteColor++ % paleta.length]);
            } catch (Exception ignored) {
                // memoria llena o variable repetida: se omite en esta demo
            }
        }
        dibujarMemoria();
        actualizarEstadoMemoria();
        mostrarSolo(panelMemoria);
    }

    @FXML
    private void asignarMemoria() {
        try {
            String nombre = campoVariableMemoria.getText().trim();
            int tamanio = Integer.parseInt(campoTamanioMemoria.getText().trim());
            if (nombre.isEmpty() || tamanio <= 0) throw new IllegalArgumentException("Datos inválidos.");
            memoria.asignar(nombre, tamanio);
            coloresPorVariable.put(nombre, paleta[siguienteColor++ % paleta.length]);
            dibujarMemoria();
            actualizarEstadoMemoria();
        } catch (Exception ex) {
            etiquetaEstadoMemoria.setText("Error: " + ex.getMessage());
        }
    }

    @FXML
    private void liberarMemoria() {
        String nombre = campoVariableMemoria.getText().trim();
        memoria.liberar(nombre);
        coloresPorVariable.remove(nombre);
        dibujarMemoria();
        actualizarEstadoMemoria();
    }

    @FXML
    private void recolectarBasura() {
        String vivasTexto = campoVariableMemoria.getText().trim();
        List<String> vivas = vivasTexto.isEmpty()
                ? List.of()
                : java.util.Arrays.asList(vivasTexto.split("\\s*,\\s*"));
        memoria.recolectarBasura(vivas);
        coloresPorVariable.keySet().retainAll(memoria.getTablaAsignacion().keySet());
        dibujarMemoria();
        actualizarEstadoMemoria();
    }

    private void dibujarMemoria() {
        cuadriculaMemoria.getChildren().clear();
        String[] celdas = memoria.getCeldas();
        for (int i = 0; i < celdas.length; i++) {
            Rectangle celda = new Rectangle(28, 28);
            String duenio = celdas[i];
            celda.setFill(duenio == null ? Color.web("#E0E0E0") : coloresPorVariable.getOrDefault(duenio, Color.GRAY));
            celda.setStroke(Color.web("#999"));
            Tooltip.install(celda, new Tooltip("Dirección " + i + (duenio != null ? " -> " + duenio : " (libre)")));
            cuadriculaMemoria.getChildren().add(celda);
        }
    }

    private void actualizarEstadoMemoria() {
        etiquetaEstadoMemoria.setText("Localidades libres: " + memoria.espacioLibreTotal() + " / " + MemoriaSimulada.TAMANIO
                + "   |   Variables en memoria: " + memoria.getTablaAsignacion().keySet());
    }

    // ==================== Volver al inicio desde cualquier fase ====================

    @FXML
    private void finalizar() {
        campoExpresion.clear();
        mostrarSolo(panelEntrada);
    }
}
