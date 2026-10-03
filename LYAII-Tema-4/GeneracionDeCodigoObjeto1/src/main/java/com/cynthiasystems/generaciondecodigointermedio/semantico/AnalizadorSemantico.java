package com.cynthiasystems.generaciondecodigointermedio.semantico;

import java.util.ArrayList;
import java.util.List;

public class AnalizadorSemantico {

    public static class Simbolo {
        public final String nombre;
        public final String tipo;
        public final int orden;

        public Simbolo(String nombre, String tipo, int orden) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.orden = orden;
        }
    }

    public static class ResultadoSemantico {
        public final List<Simbolo> tablaSimbolos;
        public final List<String> errores;
        public final boolean valido;

        ResultadoSemantico(List<Simbolo> tablaSimbolos, List<String> errores) {
            this.tablaSimbolos = tablaSimbolos;
            this.errores = errores;
            this.valido = errores.isEmpty();
        }
    }

    public ResultadoSemantico analizar(String expresionFuente, List<String> postfija, List<String> variables) {
        List<String> errores = new ArrayList<>();
        List<Simbolo> tabla = new ArrayList<>();

        if (postfija.isEmpty()) {
            errores.add("La expresión está vacía: no hay nada que analizar.");
        }

        // Tabla de símbolos: toda variable válida se infiere de tipo "entero",
        // ya que este lenguaje de juguete solo opera con expresiones numéricas.
        int orden = 1;
        for (String variable : variables) {
            if (!variable.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                errores.add("Identificador no válido: '" + variable + "'. Debe iniciar con una letra o guión bajo.");
            } else {
                tabla.add(new Simbolo(variable, "entero", orden++));
            }
        }

        String sinEspacios = expresionFuente.replaceAll("\\s+", "");

        // División entre literal 0 (ej. "a / 0")
        if (sinEspacios.matches(".*/0([^0-9].*|)$")) {
            errores.add("División entre cero detectada en la expresión.");
        }

        // Operadores consecutivos no válidos (ej. "a + * b")
        if (sinEspacios.matches(".*[+\\-*/]{2,}.*")) {
            errores.add("Se encontraron operadores consecutivos no válidos.");
        }

        // Paréntesis balanceados
        int balance = 0;
        for (char c : sinEspacios.toCharArray()) {
            if (c == '(') balance++;
            if (c == ')') balance--;
            if (balance < 0) break;
        }
        if (balance != 0) {
            errores.add("Los paréntesis no están balanceados.");
        }

        return new ResultadoSemantico(tabla, errores);
    }
}
