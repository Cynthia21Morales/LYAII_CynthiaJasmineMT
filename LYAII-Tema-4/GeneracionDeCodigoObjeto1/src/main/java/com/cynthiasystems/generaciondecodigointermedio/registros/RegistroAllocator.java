package com.cynthiasystems.generaciondecodigointermedio.registros;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class RegistroAllocator {

    public static class Resultado {
        public final List<String> postfija;
        public final List<String> instrucciones;
        public final int registrosUsados;

        Resultado(List<String> postfija, List<String> instrucciones, int registrosUsados) {
            this.postfija = postfija;
            this.instrucciones = instrucciones;
            this.registrosUsados = registrosUsados;
        }
    }
    private static int precedencia(String op) {
        return switch (op) {
            case "+", "-" -> 1;
            case "*", "/" -> 2;
            default -> 0;
        };
    }
    public static boolean esOperador(String token) {
        return token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/");
    }

    public List<String> extraerVariables(String expresionInfija) {
        List<String> variables = new ArrayList<>();
        for (String token : aPostfija(expresionInfija)) {
            if (!esOperador(token) && !token.matches("-?\\d+") && !variables.contains(token)) {
                variables.add(token);
            }
        }
        return variables;
    }

    public List<String> aPostfija(String expresionInfija) {
        List<String> salida = new ArrayList<>();
        Deque<String> pilaOps = new ArrayDeque<>();
        String limpia = expresionInfija.replace("(", " ( ").replace(")", " ) ")
                .replace("+", " + ").replace("-", " - ")
                .replace("*", " * ").replace("/", " / ");

        for (String token : limpia.trim().split("\\s+")) {
            if (token.isEmpty()) continue;
            if (token.equals("(")) {
                pilaOps.push(token);
            } else if (token.equals(")")) {
                while (!pilaOps.isEmpty() && !pilaOps.peek().equals("(")) {
                    salida.add(pilaOps.pop());
                }
                if (!pilaOps.isEmpty()) pilaOps.pop();
            } else if (esOperador(token)) {
                while (!pilaOps.isEmpty() && esOperador(pilaOps.peek())
                        && precedencia(pilaOps.peek()) >= precedencia(token)) {
                    salida.add(pilaOps.pop());
                }
                pilaOps.push(token);
            } else {
                salida.add(token); // operando (variable o número)
            }
        }
        while (!pilaOps.isEmpty()) {
            salida.add(pilaOps.pop());
        }
        return salida;
    }

    public Resultado generarCodigo(String expresionInfija) {
        List<String> postfija = aPostfija(expresionInfija);
        List<String> instrucciones = new ArrayList<>();

        Deque<Integer> registrosLibres = new ArrayDeque<>();
        for (int i = 8; i >= 1; i--) registrosLibres.push(i); // R1..R8 disponibles
        Deque<Integer> pilaRegistros = new ArrayDeque<>();
        int maxRegistroUsado = 0;

        for (String token : postfija) {
            if (esOperador(token)) {
                int rDerecho = pilaRegistros.pop();
                int rIzquierdo = pilaRegistros.pop();
                String mnemonico = switch (token) {
                    case "+" -> "ADD";
                    case "-" -> "SUB";
                    case "*" -> "MUL";
                    default -> "DIV";
                };
                instrucciones.add(mnemonico + " R" + rIzquierdo + ", R" + rDerecho + " -> R" + rIzquierdo);
                registrosLibres.push(rDerecho); // se libera el registro derecho
                pilaRegistros.push(rIzquierdo);
            } else {
                if (registrosLibres.isEmpty()) {
                    instrucciones.add("¡Sin registros libres! Se necesitaría acceso a memoria (spill) para " + token);
                    continue;
                }
                int r = registrosLibres.pop();
                maxRegistroUsado = Math.max(maxRegistroUsado, r);
                instrucciones.add("LOAD " + token + " -> R" + r);
                pilaRegistros.push(r);
            }
        }
        return new Resultado(postfija, instrucciones, maxRegistroUsado);
    }
}
