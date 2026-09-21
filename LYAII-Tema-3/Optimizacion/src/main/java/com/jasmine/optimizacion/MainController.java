package com.jasmine.optimizacion;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.*;
import java.util.regex.*;

public class MainController {
    @FXML
    private TextArea areaEntrada;
    @FXML
    private TextArea areaResultado;
    @FXML
    private ComboBox<String> cbOptimizacion;
    @FXML
    private ComboBox<String> cbCosto;

    private static final Pattern ASIG =
            Pattern.compile("^\\s*([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*(.+?)\\s*;?\\s*$");

    @FXML
    private void initialize() {
        cbOptimizacion.getItems().addAll(
                "Optimización local", "Optimización de ciclos",
                "Optimización global", "Optimización de mirilla",
                "Costo de ejecución", "Criterios para mejorar el código",
                "Análisis de flujo de datos"
        );
        cbOptimizacion.getSelectionModel().selectFirst();

        cbCosto.getItems().addAll("Memoria", "Registros", "Pila", "Comparación de costos");
        cbCosto.getSelectionModel().select("Comparación de costos");
    }

    @FXML
    private void optimizar() {
        String codigo = areaEntrada.getText();
        if (codigo == null || codigo.isBlank()) {
            alerta("Escribe primero el código que deseas optimizar.");
            return;
        }

        String tipo = cbOptimizacion.getValue();
        String resultado = switch (tipo) {
            case "Optimización local" -> local(codigo);
            case "Optimización de ciclos" -> ciclos(codigo);
            case "Optimización global" -> global(codigo);
            case "Optimización de mirilla" -> mirilla(codigo);
            case "Costo de ejecución" -> costo(codigo);
            case "Criterios para mejorar el código" -> criterios(codigo);
            case "Análisis de flujo de datos" -> flujo(codigo);
            default -> codigo;
        };
        areaResultado.setText(resultado);
    }

    @FXML
    private void analizarCosto() {
        if (areaEntrada.getText().isBlank()) {
            alerta("Escribe primero el código que deseas analizar.");
            return;
        }
        cbOptimizacion.getSelectionModel().select("Costo de ejecución");
        areaResultado.setText(costo(areaEntrada.getText()));
    }

    @FXML
    private void limpiar() {
        areaEntrada.clear();
        areaResultado.clear();
        cbOptimizacion.getSelectionModel().selectFirst();
        cbCosto.getSelectionModel().select("Comparación de costos");
    }

    private String local(String code) {
        List<String> out = new ArrayList<>();
        for (String s : lines(code)) {
            if (s.isBlank()) continue;
            s = constants(s);
            s = identities(s);
            if (!s.isBlank()) out.add(s);
        }
        return String.join("\n", out);
    }

    private String ciclos(String code) {
        List<String> salida = new ArrayList<>();

        for (String linea : lines(code)) {
            String actual = linea.trim();

            if (actual.isBlank()) {
                continue;
            }

            // Optimización sencilla dentro de ciclos:
            // elimina operaciones innecesarias como * 1, + 0, - 0, etc.
            actual = identities(actual);

            // Evalúa operaciones constantes.
            actual = constants(actual);

            if (!actual.isBlank()) {
                salida.add(actual);
            }
        }

        return String.join("\n", salida);
    }

    private String cycles(String code) {
        List<String> out = new ArrayList<>();
        for (String s : lines(code)) {
            if (!s.isBlank()) out.add(identities(constants(s)));
        }
        return String.join("\n", out);
    }

    private String global(String code) {
        Map<String, String> known = new LinkedHashMap<>();
        List<String> out = new ArrayList<>();

        for (String s : lines(code)) {
            if (s.isBlank()) continue;
            Matcher m = ASIG.matcher(s.trim());
            if (!m.matches()) {
                out.add(replaceVars(s.trim(), known));
                continue;
            }

            String var = m.group(1);
            String expr = replaceVars(m.group(2), known);
            expr = identities(constants(expr));

            if (expr.matches("-?\\d+(?:\\.\\d+)?|[A-Za-z_][A-Za-z0-9_]*"))
                known.put(var, expr);
            else
                known.remove(var);

            out.add(var + " = " + expr);
        }
        return String.join("\n", out);
    }

    private String mirilla(String code) {
        List<String> out = new ArrayList<>(lines(code));
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < out.size(); i++) {
                String old = out.get(i);
                String n = identities(constants(old.trim()));
                if (!n.equals(old.trim())) {
                    out.set(i, n);
                    changed = true;
                }
                Matcher m = ASIG.matcher(out.get(i));
                if (m.matches() && m.group(1).equals(m.group(2).trim())) {
                    out.remove(i--);
                    changed = true;
                }
            }
        } while (changed);
        return String.join("\n", out);
    }

    private String costo(String code) {
        int ins = 0, ops = 0, nums = 0;
        Set<String> vars = new LinkedHashSet<>();

        for (String s : lines(code)) {
            s = s.trim();
            if (s.isBlank()) continue;
            ins++;
            ops += count("[+\\-*/=<>]", s);
            nums += count("\\b\\d+(?:\\.\\d+)?\\b", s);

            Matcher m = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*").matcher(s);
            while (m.find()) {
                String v = m.group();
                if (!Set.of("if", "else", "for", "while", "return").contains(v)) vars.add(v);
            }
        }

        int memoria = vars.size() + nums;
        int registros = Math.max(1, Math.min(vars.size(), 8));
        int pila = ins + ops;
        int total = ins + ops + memoria;
        String nivel = total <= 6 ? "Bajo" : total <= 12 ? "Medio" : "Alto";

        return "Instrucciones: " + ins +
                "\nOperaciones: " + ops +
                "\nVariables/referencias: " + vars.size() +
                "\nConstantes: " + nums +
                "\nCosto estimado de memoria: " + memoria +
                "\nUso estimado de registros: " + registros +
                "\nOperaciones estimadas de pila: " + pila +
                "\nCosto general estimado: " + nivel;
    }

    private String criterios(String code) {
        String opt = global(code);
        int a = countLines(code), b = countLines(opt);
        return opt + "\n\nInstrucciones antes: " + a +
                "\nInstrucciones después: " + b +
                "\nReducción: " + Math.max(0, a - b);
    }

    private String flujo(String code) {
        Set<String> defs = new LinkedHashSet<>(), uses = new LinkedHashSet<>();
        List<String> out = new ArrayList<>();
        String[] ls = code.split("\\R", -1);

        for (int i = 0; i < ls.length; i++) {
            String s = ls[i].trim();
            if (s.isBlank()) continue;
            Set<String> gen = vars(s);
            Set<String> kill = new LinkedHashSet<>();
            Matcher m = ASIG.matcher(s);
            if (m.matches()) {
                kill.add(m.group(1));
                defs.add(m.group(1));
                gen.remove(m.group(1));
            }
            uses.addAll(gen);
            out.add("Línea " + (i + 1) + " | GEN: " + fmt(gen) + " | KILL: " + fmt(kill));
        }

        Set<String> undef = new LinkedHashSet<>(uses);
        undef.removeAll(defs);
        out.add("");
        out.add("Variables definidas: " + fmt(defs));
        out.add("Variables usadas: " + fmt(uses));
        out.add("Referencias sin definición detectada: " + fmt(undef));
        return String.join("\n", out);
    }

    private String constants(String s) {
        Pattern p = Pattern.compile("(?<![A-Za-z0-9_])(-?\\d+)\\s*([+\\-*/])\\s*(-?\\d+)(?![A-Za-z0-9_])");
        boolean ch;
        do {
            ch = false;
            Matcher m = p.matcher(s);
            if (m.find()) {
                long a = Long.parseLong(m.group(1)), b = Long.parseLong(m.group(3));
                Long r = switch (m.group(2)) {
                    case "+" -> a + b;
                    case "-" -> a - b;
                    case "*" -> a * b;
                    case "/" -> b != 0 ? a / b : null;
                    default -> null;
                };
                if (r != null) {
                    s = m.replaceFirst(String.valueOf(r));
                    ch = true;
                }
            }
        } while (ch);
        return s;
    }

    private String identities(String s) {
        String[][] r = {
                {"\\b([A-Za-z_][A-Za-z0-9_]*)\\s*\\+\\s*0\\b", "$1"},
                {"\\b0\\s*\\+\\s*([A-Za-z_][A-Za-z0-9_]*)\\b", "$1"},
                {"\\b([A-Za-z_][A-Za-z0-9_]*)\\s*-\\s*0\\b", "$1"},
                {"\\b([A-Za-z_][A-Za-z0-9_]*)\\s*\\*\\s*1\\b", "$1"},
                {"\\b1\\s*\\*\\s*([A-Za-z_][A-Za-z0-9_]*)\\b", "$1"},
                {"\\b([A-Za-z_][A-Za-z0-9_]*)\\s*/\\s*1\\b", "$1"},
                {"\\b([A-Za-z_][A-Za-z0-9_]*)\\s*\\*\\s*0\\b", "0"},
                {"\\b0\\s*\\*\\s*([A-Za-z_][A-Za-z0-9_]*)\\b", "0"}
        };
        for (String[] x : r) s = s.replaceAll(x[0], x[1]);
        return s;
    }

    private String replaceVars(String s, Map<String, String> map) {
        List<String> keys = new ArrayList<>(map.keySet());
        keys.sort(Comparator.comparingInt(String::length).reversed());
        for (String k : keys)
            s = s.replaceAll("(?<![A-Za-z0-9_])" + Pattern.quote(k) + "(?![A-Za-z0-9_])",
                    Matcher.quoteReplacement(map.get(k)));
        return s;
    }

    private Set<String> vars(String s) {
        Set<String> r = new LinkedHashSet<>();
        Matcher m = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*").matcher(s);
        while (m.find()) {
            String v = m.group();
            if (!Set.of("if", "else", "for", "while", "return").contains(v)) r.add(v);
        }
        return r;
    }

    private int count(String regex, String s) {
        Matcher m = Pattern.compile(regex).matcher(s);
        int n = 0;
        while (m.find()) n++;
        return n;
    }

    private int countLines(String s) {
        int n = 0;
        for (String x : lines(s)) if (!x.isBlank()) n++;
        return n;
    }

    private List<String> lines(String s) {
        return Arrays.asList(s.split("\\R"));
    }

    private String fmt(Collection<String> s) {
        return s.isEmpty() ? "{}" : "{ " + String.join(", ", s) + " }";
    }

    private void alerta(String s) {
        new Alert(Alert.AlertType.WARNING, s, ButtonType.OK).showAndWait();
    }
}
