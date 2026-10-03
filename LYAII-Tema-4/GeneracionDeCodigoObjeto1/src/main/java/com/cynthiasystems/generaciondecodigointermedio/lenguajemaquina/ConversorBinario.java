package com.cynthiasystems.generaciondecodigointermedio.lenguajemaquina;

public class ConversorBinario {

    public String decimalABinario(long decimal) {
        if (decimal == 0) return "0";
        return Long.toBinaryString(decimal);
    }

    public long binarioADecimal(String binario) {
        return Long.parseLong(binario.trim(), 2);
    }

    public String decimalAHex(long decimal) {
        return Long.toHexString(decimal).toUpperCase();
    }

    public String binarioAHex(String binario) {
        return decimalAHex(binarioADecimal(binario));
    }

    /** Convierte una cadena hexadecimal a binario, rellenando con ceros a la izquierda (4 bits por dígito hex). */
    public String hexABinario(String hex) {
        long valor = Long.parseLong(hex.trim(), 16);
        String binario = Long.toBinaryString(valor);
        int bitsEsperados = hex.trim().length() * 4;
        return "0".repeat(Math.max(0, bitsEsperados - binario.length())) + binario;
    }

    public String interpretarInstruccion(String binario8bits) {
        String limpio = binario8bits.replace(" ", "");
        if (!limpio.matches("[01]{8}")) {
            return "Se necesitan exactamente 8 bits (0 y 1) para interpretar la instrucción.";
        }
        String campoOpcode = limpio.substring(0, 4);
        String campoOperando = limpio.substring(4, 8);
        int opcode = Integer.parseInt(campoOpcode, 2);
        int operando = Integer.parseInt(campoOperando, 2);

        String mnemonico = switch (opcode) {
            case 0 -> "HALT";
            case 1 -> "LOAD";
            case 2 -> "STORE";
            case 3 -> "ADD";
            case 4 -> "SUB";
            case 5 -> "MUL";
            case 6 -> "DIV";
            case 7 -> "MOV";
            case 8 -> "JMP";
            default -> "OP" + opcode;
        };
        return mnemonico + " " + operando + "   (opcode=" + campoOpcode + ", operando=" + campoOperando + ")";
    }
}
