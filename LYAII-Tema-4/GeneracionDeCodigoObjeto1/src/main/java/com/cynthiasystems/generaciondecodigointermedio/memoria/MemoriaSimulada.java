package com.cynthiasystems.generaciondecodigointermedio.memoria;

import java.util.LinkedHashMap;
import java.util.Map;

public class MemoriaSimulada {

    public static final int TAMANIO = 64; // 64 localidades de 1 byte

    private final String[] celdas = new String[TAMANIO]; // nombre de variable dueña, o null si libre
    private final Map<String, int[]> tablaAsignacion = new LinkedHashMap<>(); // variable -> [inicio, tamaño]

    public String[] getCeldas() {
        return celdas;
    }

    public Map<String, int[]> getTablaAsignacion() {
        return tablaAsignacion;
    }

    /** Busca el primer hueco libre contiguo de tamaño suficiente (first-fit). */
    public int asignar(String variable, int tamanio) {
        if (tablaAsignacion.containsKey(variable)) {
            throw new IllegalArgumentException("La variable '" + variable + "' ya existe en memoria.");
        }
        int libresSeguidas = 0;
        int inicioCandidato = -1;
        for (int i = 0; i < TAMANIO; i++) {
            if (celdas[i] == null) {
                if (libresSeguidas == 0) inicioCandidato = i;
                libresSeguidas++;
                if (libresSeguidas == tamanio) {
                    for (int j = inicioCandidato; j < inicioCandidato + tamanio; j++) {
                        celdas[j] = variable;
                    }
                    tablaAsignacion.put(variable, new int[]{inicioCandidato, tamanio});
                    return inicioCandidato;
                }
            } else {
                libresSeguidas = 0;
            }
        }
        throw new IllegalStateException("No hay memoria contigua suficiente para '" + variable + "'.");
    }

    /** Libera manualmente el bloque de una variable (equivalente a un free explícito). */
    public void liberar(String variable) {
        int[] datos = tablaAsignacion.remove(variable);
        if (datos == null) return;
        for (int j = datos[0]; j < datos[0] + datos[1]; j++) {
            celdas[j] = null;
        }
    }

    public void recolectarBasura(Iterable<String> variablesVivas) {
        java.util.Set<String> vivas = new java.util.HashSet<>();
        variablesVivas.forEach(vivas::add);
        for (String variable : new java.util.ArrayList<>(tablaAsignacion.keySet())) {
            if (!vivas.contains(variable)) {
                liberar(variable);
            }
        }
    }

    public int espacioLibreTotal() {
        int libres = 0;
        for (String c : celdas) if (c == null) libres++;
        return libres;
    }
}
