package com.jasmine.codigointermedio;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainController {

    @FXML
    private TextField txtExpresion;

    @FXML
    private TextArea areaResultado;

    @FXML
    private ComboBox<String> cbRepresentacion;


    @FXML
    private void initialize() {

        cbRepresentacion.getItems().addAll(
                "Prefija",
                "Infija",
                "Postfija",
                "Notación Polaca",
                "Código P",
                "Triplos",
                "Cuádruplos"
        );

        cbRepresentacion.getSelectionModel().select("Postfija");

        areaResultado.clear();
    }


    @FXML
    private void convertir() {

        String expresion = txtExpresion.getText().trim();

        if (expresion.isEmpty()) {
            alerta("Escribe una expresión.");
            return;
        }

        String infija = expresion.replaceAll("\\s+", "");

        String postfija = postfija(infija);
        String prefija = prefija(infija);

        String representacion = cbRepresentacion.getValue();

        String resultado = "";

        switch (representacion) {

            case "Prefija":
                resultado = prefija;
                break;

            case "Infija":
                resultado = infija;
                break;

            case "Postfija":
                resultado = postfija;
                break;

            case "Notación Polaca":
                resultado = prefija;
                break;

            case "Código P":
                resultado = codigoP(postfija);
                break;

            case "Triplos":
                resultado = triplos(postfija);
                break;

            case "Cuádruplos":
                resultado = cuadruplos(postfija);
                break;
        }

        areaResultado.setText(resultado);
    }


    @FXML
    private void limpiar() {

        txtExpresion.clear();
        areaResultado.clear();

        cbRepresentacion.getSelectionModel().select("Postfija");
    }


    private void alerta(String mensaje) {

        new Alert(
                Alert.AlertType.WARNING,
                mensaje,
                ButtonType.OK
        ).showAndWait();
    }


    // ==========================================
    // PRIORIDAD DE OPERADORES
    // ==========================================

    private int prioridad(String operador) {

        if (operador.equals("+") || operador.equals("-")) {
            return 1;
        }

        if (operador.equals("*") || operador.equals("/")) {
            return 2;
        }

        if (operador.equals("^")) {
            return 3;
        }

        if (operador.equals("=")) {
            return 0;
        }

        return -1;
    }


    // ==========================================
    // SEPARAR TOKENS
    // ==========================================

    private List<String> tokens(String expresion) {

        List<String> resultado = new ArrayList<>();

        Pattern patron = Pattern.compile(
                "[A-Za-z_][A-Za-z0-9_]*|\\d+(?:\\.\\d+)?|[()+\\-*/^=]"
        );

        Matcher matcher = patron.matcher(expresion);

        while (matcher.find()) {
            resultado.add(matcher.group());
        }

        return resultado;
    }


    // ==========================================
    // CONVERTIR A POSTFIJA
    // ==========================================

    private String postfija(String expresion) {

        Stack<String> pila = new Stack<>();

        List<String> salida = new ArrayList<>();

        for (String token : tokens(expresion)) {

            // Variable o número
            if (token.matches(
                    "[A-Za-z_][A-Za-z0-9_]*|\\d+(?:\\.\\d+)?"
            )) {

                salida.add(token);
            }

            // Paréntesis izquierdo
            else if (token.equals("(")) {

                pila.push(token);
            }

            // Paréntesis derecho
            else if (token.equals(")")) {

                while (!pila.isEmpty()
                        && !pila.peek().equals("(")) {

                    salida.add(pila.pop());
                }

                if (!pila.isEmpty()) {
                    pila.pop();
                }
            }

            // Operador
            else {

                while (!pila.isEmpty()
                        && !pila.peek().equals("(")
                        && prioridad(pila.peek())
                        >= prioridad(token)) {

                    salida.add(pila.pop());
                }

                pila.push(token);
            }
        }

        while (!pila.isEmpty()) {
            salida.add(pila.pop());
        }

        return String.join(" ", salida);
    }


    // ==========================================
    // CONVERTIR A PREFIJA
    // ==========================================

    private String prefija(String expresion) {

        List<String> tokens = tokens(expresion);

        Collections.reverse(tokens);

        for (int i = 0; i < tokens.size(); i++) {

            if (tokens.get(i).equals("(")) {

                tokens.set(i, ")");

            } else if (tokens.get(i).equals(")")) {

                tokens.set(i, "(");
            }
        }

        String expresionInvertida = String.join("", tokens);

        String resultadoPostfijo =
                postfija(expresionInvertida);

        List<String> resultado = new ArrayList<>(
                Arrays.asList(resultadoPostfijo.split(" "))
        );

        Collections.reverse(resultado);

        return String.join(" ", resultado);
    }


    // ==========================================
    // CÓDIGO P
    // ==========================================

    private String codigoP(String postfija) {

        StringBuilder resultado = new StringBuilder();

        for (String token : postfija.split(" ")) {

            if ("+-*/^=".contains(token)) {

                resultado.append("OP ")
                        .append(token)
                        .append("\n");

            } else {

                resultado.append("PUSH ")
                        .append(token)
                        .append("\n");
            }
        }

        resultado.append("POP resultado");

        return resultado.toString();
    }


    // ==========================================
    // TRIPLOS
    // ==========================================

    private String triplos(String postfija) {

        StringBuilder resultado = new StringBuilder();

        resultado.append(
                "Índice | Operador | Arg1 | Arg2\n"
        );

        Stack<String> pila = new Stack<>();

        int indice = 0;

        for (String token : postfija.split(" ")) {

            if ("+-*/^=".contains(token)
                    && pila.size() >= 2) {

                String arg2 = pila.pop();
                String arg1 = pila.pop();

                resultado
                        .append(indice)
                        .append(" | ")
                        .append(token)
                        .append(" | ")
                        .append(arg1)
                        .append(" | ")
                        .append(arg2)
                        .append("\n");

                pila.push("(" + indice + ")");

                indice++;

            } else {

                pila.push(token);
            }
        }

        return resultado.toString();
    }


    // ==========================================
    // CUÁDRUPLOS
    // ==========================================

    private String cuadruplos(String postfija) {

        StringBuilder resultado = new StringBuilder();

        resultado.append(
                "Índice | Operador | Arg1 | Arg2 | Resultado\n"
        );

        Stack<String> pila = new Stack<>();

        int indice = 0;

        for (String token : postfija.split(" ")) {

            if ("+-*/^=".contains(token)
                    && pila.size() >= 2) {

                String arg2 = pila.pop();
                String arg1 = pila.pop();

                String temporal = "T" + indice;

                resultado
                        .append(indice)
                        .append(" | ")
                        .append(token)
                        .append(" | ")
                        .append(arg1)
                        .append(" | ")
                        .append(arg2)
                        .append(" | ")
                        .append(temporal)
                        .append("\n");

                pila.push(temporal);

                indice++;

            } else {

                pila.push(token);
            }
        }

        return resultado.toString();
    }
}