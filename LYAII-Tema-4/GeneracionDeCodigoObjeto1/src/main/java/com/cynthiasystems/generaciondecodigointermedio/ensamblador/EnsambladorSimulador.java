package com.cynthiasystems.generaciondecodigointermedio.ensamblador;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EnsambladorSimulador {

    private static final Map<String, Integer> OPCODES = new LinkedHashMap<>();
    static {
        OPCODES.put("HALT", 0x00);
        OPCODES.put("LOAD", 0x01);
        OPCODES.put("STORE", 0x02);
        OPCODES.put("ADD", 0x03);
        OPCODES.put("SUB", 0x04);
        OPCODES.put("MUL", 0x05);
        OPCODES.put("DIV", 0x06);
        OPCODES.put("MOV", 0x07);
        OPCODES.put("JMP", 0x08);
    }

    public static class LineaEnsamblada {
        public final int direccion;
        public final String etiqueta;
        public final String fuente;
        public final String codigoMaquina;

        LineaEnsamblada(int direccion, String etiqueta, String fuente, String codigoMaquina) {
            this.direccion = direccion;
            this.etiqueta = etiqueta;
            this.fuente = fuente;
            this.codigoMaquina = codigoMaquina;
        }
    }

    public List<LineaEnsamblada> ensamblar(String codigoFuente) {
        List<String> lineasCrudas = new ArrayList<>();
        Map<String, Integer> tablaSimbolos = new LinkedHashMap<>();

        // (cada etiqueta debe ir en la misma línea que su instrucción, ej: "INICIO: LOAD A, R1")
        for (String linea : codigoFuente.split("\n")) {
            String limpia = linea.split(";")[0].trim();
            if (limpia.isEmpty()) continue;
            String etiqueta = null;
            if (limpia.contains(":")) {
                String[] partes = limpia.split(":", 2);
                etiqueta = partes[0].trim();
                limpia = partes[1].trim();
                tablaSimbolos.put(etiqueta, lineasCrudas.size());
            }
            lineasCrudas.add((etiqueta != null ? etiqueta + ":" : "") + limpia);
        }

        // Segunda pasada: traducir cada instrucción a código máquina
        List<LineaEnsamblada> resultado = new ArrayList<>();
        int direccion = 0;
        for (String linea : lineasCrudas) {
            String etiqueta = null;
            String instruccion = linea;
            if (linea.contains(":")) {
                String[] partes = linea.split(":", 2);
                etiqueta = partes[0].trim();
                instruccion = partes[1].trim();
            }
            String[] tokens = instruccion.split("\\s+", 2);
            String mnemonico = tokens[0].toUpperCase();
            String operandos = tokens.length > 1 ? tokens[1] : "";

            Integer opcode = OPCODES.get(mnemonico);
            String hex;
            if (opcode == null) {
                hex = "ERROR: mnemónico desconocido";
            } else {
                int operandoResuelto = resolverOperando(operandos, tablaSimbolos);
                hex = String.format("%02X%02X", opcode, operandoResuelto & 0xFF);
            }
            resultado.add(new LineaEnsamblada(direccion, etiqueta, instruccion, hex));
            direccion++;
        }
        return resultado;
    }

    private int resolverOperando(String operandos, Map<String, Integer> tablaSimbolos) {
        if (operandos.isEmpty()) return 0;
        String primerOperando = operandos.split(",")[0].trim();
        if (tablaSimbolos.containsKey(primerOperando)) {
            return tablaSimbolos.get(primerOperando); // referencia simbólica resuelta a dirección
        }
        if (primerOperando.matches("-?\\d+")) {
            return Integer.parseInt(primerOperando);
        }
        // Registro (Rn) o variable: se usa un valor derivado del nombre, solo ilustrativo
        return Math.abs(primerOperando.hashCode()) % 100;
    }
}
