package com.cynthiasystems.controller;

import com.cynthiasystems.model.ExpressionRow;
import com.cynthiasystems.model.Symbol;
import com.cynthiasystems.service.SemanticAnalyzer;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;


public class MainController {

    @FXML
    private TextArea sourceArea;

    @FXML
    private TextArea outputArea;

    @FXML
    private TextArea treeArea;

    @FXML
    private TextArea stackArea;

    @FXML
    private TextArea translationArea;

    @FXML
    private TableView<Symbol> symbolTable;

    @FXML
    private TableView<ExpressionRow> expressionTable;

    @FXML
    private TableColumn<Symbol, String> nameCol;

    @FXML
    private TableColumn<Symbol, String> typeCol;

    @FXML
    private TableColumn<Symbol, String> addressCol;

    @FXML
    private TableColumn<Symbol, String> valueCol;

    @FXML
    private TableColumn<ExpressionRow, String> exprCol;

    @FXML
    private TableColumn<ExpressionRow, String> exprTypeCol;

    @FXML
    private TableColumn<ExpressionRow, String> resultCol;

    @FXML
    private TableColumn<ExpressionRow, String> statusCol;

    private final SemanticAnalyzer analyzer = new SemanticAnalyzer();

    @FXML
    public void initialize() {

        nameCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().name()));

        typeCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().type()));

        addressCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().address()));

        valueCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().value()));

        exprCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().expression()));

        exprTypeCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().type()));

        resultCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().result()));

        statusCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().status()));
    }

    @FXML
    private void analyze() {

        analyzer.analyze(sourceArea.getText());

        symbolTable.setItems(
                FXCollections.observableArrayList(
                        analyzer.symbols().values()
                )
        );

        expressionTable.setItems(
                FXCollections.observableArrayList(
                        analyzer.expressions()
                )
        );

        // Resultados del análisis
        if (analyzer.errors().isEmpty()) {

            outputArea.setText(
                    "Análisis semántico correcto.\n"
                            + "No se encontraron errores."
            );

        } else {

            outputArea.setText(
                    String.join(
                            "\n",
                            analyzer.errors()
                    )
            );
        }

        // Pila semántica
        stackArea.setText(
                "Pila semántica\n"
                        + "-------------------------\n"
                        + String.join(
                        "\n",
                        analyzer.stack()
                )
        );

        // Esquema de traducción
        translationArea.setText(
                "Esquema de traducción / notación postfija\n"
                        + "----------------------------------------\n"
                        + String.join(
                        "\n",
                        analyzer.translation()
                )
        );

        // Árbol de la última expresión
        if (!analyzer.expressions().isEmpty()) {

            ExpressionRow lastExpression =
                    analyzer.expressions().get(
                            analyzer.expressions().size() - 1
                    );

            treeArea.setText(
                    analyzer.buildExpressionTree(
                            lastExpression.expression()
                    )
            );

        } else {

            treeArea.clear();
        }
    }

    @FXML
    private void tree() {

        String source = sourceArea.getText();

        if (source == null || source.isBlank()) {
            treeArea.setText(
                    "No hay una expresión para analizar."
            );
            return;
        }

        String[] lines = source.split("\\R");

        String expression = null;

        // Buscar la última asignación
        for (int i = lines.length - 1; i >= 0; i--) {

            String line = lines[i].trim();

            if (line.contains("=")) {

                int equal = line.indexOf('=');

                if (equal >= 0 && equal < line.length() - 1) {

                    expression = line
                            .substring(equal + 1)
                            .replace(";", "")
                            .trim();

                    break;
                }
            }
        }

        if (expression != null && !expression.isBlank()) {

            treeArea.setText(
                    analyzer.buildExpressionTree(expression)
            );

        } else {

            treeArea.setText(
                    "No se encontró una expresión."
            );
        }
    }

    @FXML
    private void clear() {

        sourceArea.clear();
        outputArea.clear();
        treeArea.clear();
        stackArea.clear();
        translationArea.clear();

        symbolTable.getItems().clear();
        expressionTable.getItems().clear();
    }

    @FXML
    private void example() {

        sourceArea.setText(
                "int a = 10;\n"
                        + "int b = 5;\n"
                        + "int c = 2;\n"
                        + "int resultado = (a + b) * c;"
        );
    }
}