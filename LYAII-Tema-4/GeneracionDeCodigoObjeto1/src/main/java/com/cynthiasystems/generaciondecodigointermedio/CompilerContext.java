package com.cynthiasystems.generaciondecodigointermedio;

import com.cynthiasystems.generaciondecodigointermedio.ensamblador.EnsambladorSimulador;
import com.cynthiasystems.generaciondecodigointermedio.registros.RegistroAllocator;

import java.util.ArrayList;
import java.util.List;

public class CompilerContext {

    public static String expresion = "a + b * c - d";
    public static List<String> variables = new ArrayList<>();
    public static List<String> postfija = new ArrayList<>();
    public static List<String> instruccionesRegistros = new ArrayList<>();
    public static int registrosUsados = 0;
    public static String programaEnsamblador = "";
    public static List<EnsambladorSimulador.LineaEnsamblada> lineasEnsambladas = new ArrayList<>();

    public static void compilar(String expresionFuente) {
        expresion = expresionFuente;

        RegistroAllocator allocator = new RegistroAllocator();
        RegistroAllocator.Resultado resultado = allocator.generarCodigo(expresionFuente);
        postfija = resultado.postfija;
        instruccionesRegistros = resultado.instrucciones;
        registrosUsados = resultado.registrosUsados;
        variables = allocator.extraerVariables(expresionFuente);

        programaEnsamblador = traducirAEnsamblador(instruccionesRegistros);

        EnsambladorSimulador simulador = new EnsambladorSimulador();
        lineasEnsambladas = simulador.ensamblar(programaEnsamblador);
    }

    private static String traducirAEnsamblador(List<String> instrucciones) {
        StringBuilder sb = new StringBuilder();
        for (String instr : instrucciones) {
            if (instr.startsWith("¡Sin registros")) continue; // caso de spill, no traducible
            String antesDeFlecha = instr.split("->")[0].trim();
            sb.append(antesDeFlecha).append("\n");
        }
        sb.append("HALT");
        return sb.toString();
    }
}
