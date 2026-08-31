package com.cynthiasystems.service;

import com.cynthiasystems.model.ExpressionRow;
import com.cynthiasystems.model.Symbol;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemanticAnalyzer {

    private final Map<String, Symbol> symbols = new LinkedHashMap<>();
    private final List<ExpressionRow> expressions = new ArrayList<>();
    private final List<String> errors = new ArrayList<>();
    private final List<String> stack = new ArrayList<>();
    private final List<String> translation = new ArrayList<>();

    private int nextAddress = 1000;

    public void analyze(String source) {

        symbols.clear();
        expressions.clear();
        errors.clear();
        stack.clear();
        translation.clear();

        nextAddress = 1000;

        if (source == null || source.isBlank()) {
            return;
        }

        String[] lines = source.split("\\R");

        for (int i = 0; i < lines.length; i++) {
            processLine(lines[i].trim(), i + 1);
        }
    }

    private void processLine(String line, int number) {

        System.out.println("LINEA RECIBIDA: [" + line + "]");

        if (line.isBlank() || line.startsWith("//")) {
            return;
        }

        // Declaración:
        // int a = 10;
        // double precio = 25.50;
        // String nombre = "Cynthia";
        // boolean activo = true;

        Pattern declarationPattern = Pattern.compile(
                "^(int|double|boolean|String)\\s+([A-Za-z_]\\w*)\\s*(?:=\\s*(.+))?;$"
        );

        Matcher declaration = declarationPattern.matcher(line);

        if (declaration.matches()) {

            String type = declaration.group(1);
            String name = declaration.group(2);
            String expression = declaration.group(3);

            if (symbols.containsKey(name)) {

                errors.add(
                        "Línea " + number +
                                ": la variable '" + name +
                                "' ya fue declarada."
                );

                return;
            }

            String value = "";

            if (expression != null && !expression.isBlank()) {

                Analysis analysis =
                        analyzeExpression(expression, number);

                if (analysis.type().equals("error")) {
                    return;
                }

                if (!compatible(type, analysis.type())) {

                    errors.add(
                            "Línea " + number +
                                    ": tipos incompatibles. Se esperaba " +
                                    type +
                                    " y se obtuvo " +
                                    analysis.type() +
                                    "."
                    );

                    return;
                }

                value = analysis.value();

                expressions.add(
                        new ExpressionRow(
                                expression,
                                analysis.type(),
                                analysis.value(),
                                "Correcta"
                        )
                );

                translation.add(
                        name + " = " + analysis.postfix()
                );
            }

            Symbol symbol = new Symbol(
                    name,
                    type,
                    "@" + nextAddress++,
                    value
            );

            symbols.put(name, symbol);

            stack.add(
                    name + " : " + type +
                            (value.isEmpty() ? "" : " = " + value)
            );

            return;
        }

        // Asignación:
        // a = 20;
        // resultado = a + b;

        Pattern assignmentPattern = Pattern.compile(
                "^([A-Za-z_]\\w*)\\s*=\\s*(.+);$"
        );

        Matcher assignment = assignmentPattern.matcher(line);

        if (assignment.matches()) {

            String name = assignment.group(1);
            String expression = assignment.group(2);

            Symbol symbol = symbols.get(name);

            if (symbol == null) {

                errors.add(
                        "Línea " + number +
                                ": identificador no declarado: '" +
                                name + "'."
                );

                return;
            }

            Analysis analysis =
                    analyzeExpression(expression, number);

            if (analysis.type().equals("error")) {
                return;
            }

            if (!compatible(symbol.type(), analysis.type())) {

                errors.add(
                        "Línea " + number +
                                ": asignación incompatible: " +
                                symbol.type() +
                                " <- " +
                                analysis.type() +
                                "."
                );

                return;
            }

            Symbol updated = new Symbol(
                    name,
                    symbol.type(),
                    symbol.address(),
                    analysis.value()
            );

            symbols.put(name, updated);

            expressions.add(
                    new ExpressionRow(
                            expression,
                            analysis.type(),
                            analysis.value(),
                            "Correcta"
                    )
            );

            translation.add(
                    name + " = " + analysis.postfix()
            );

            stack.add(
                    name + " = " + analysis.value()
            );

            return;
        }

        errors.add(
                "Línea " + number +
                        ": instrucción no reconocida."
        );
    }

    private boolean compatible(String expected, String actual) {

        if (expected.equals(actual)) {
            return true;
        }

        // Permitir asignar int a double
        return expected.equals("double")
                && actual.equals("int");
    }

    private record Analysis(
            String type,
            String value,
            String postfix,
            Node node
    ) {
    }

    private record Token(
            String text,
            Kind kind
    ) {
    }

    private enum Kind {
        NUMBER,
        STRING,
        BOOL,
        ID,
        OP,
        LP,
        RP
    }

    public static class Node {

        final String value;
        final Node left;
        final Node right;

        Node(String value, Node left, Node right) {

            this.value = value;
            this.left = left;
            this.right = right;
        }
    }

    private Analysis analyzeExpression(
            String expression,
            int line
    ) {

        try {

            List<Token> tokens =
                    tokenize(expression);

            if (tokens.isEmpty()) {

                throw new IllegalArgumentException(
                        "Expresión vacía."
                );
            }

            List<Token> postfix =
                    toPostfix(tokens);

            Deque<Node> nodeStack =
                    new ArrayDeque<>();

            Deque<String> typeStack =
                    new ArrayDeque<>();

            for (Token token : postfix) {

                switch (token.kind()) {

                    case NUMBER -> {

                        String type =
                                token.text().contains(".")
                                        ? "double"
                                        : "int";

                        typeStack.push(type);

                        nodeStack.push(
                                new Node(
                                        token.text(),
                                        null,
                                        null
                                )
                        );
                    }

                    case STRING -> {

                        typeStack.push("String");

                        nodeStack.push(
                                new Node(
                                        token.text(),
                                        null,
                                        null
                                )
                        );
                    }

                    case BOOL -> {

                        typeStack.push("boolean");

                        nodeStack.push(
                                new Node(
                                        token.text(),
                                        null,
                                        null
                                )
                        );
                    }

                    case ID -> {

                        Symbol symbol =
                                symbols.get(token.text());

                        if (symbol == null) {

                            throw new IllegalArgumentException(
                                    "Identificador no declarado: '" +
                                            token.text() +
                                            "'."
                            );
                        }

                        typeStack.push(
                                symbol.type()
                        );

                        nodeStack.push(
                                new Node(
                                        token.text(),
                                        null,
                                        null
                                )
                        );
                    }

                    case OP -> {

                        if (typeStack.size() < 2
                                || nodeStack.size() < 2) {

                            throw new IllegalArgumentException(
                                    "Expresión incompleta."
                            );
                        }

                        String rightType =
                                typeStack.pop();

                        String leftType =
                                typeStack.pop();

                        Node right =
                                nodeStack.pop();

                        Node left =
                                nodeStack.pop();

                        String result =
                                resultType(
                                        leftType,
                                        rightType,
                                        token.text()
                                );

                        if (result.equals("error")) {

                            throw new IllegalArgumentException(
                                    "Operador '" +
                                            token.text() +
                                            "' incompatible con " +
                                            leftType +
                                            " y " +
                                            rightType +
                                            "."
                            );
                        }

                        typeStack.push(result);

                        nodeStack.push(
                                new Node(
                                        token.text(),
                                        left,
                                        right
                                )
                        );
                    }

                    default -> {
                        // Los paréntesis ya fueron procesados
                        // durante la conversión a postfija.
                    }
                }
            }

            if (typeStack.size() != 1
                    || nodeStack.size() != 1) {

                throw new IllegalArgumentException(
                        "Expresión inválida."
                );
            }

            String type =
                    typeStack.pop();

            Node root =
                    nodeStack.pop();

            String value =
                    evaluate(expression, type);

            return new Analysis(
                    type,
                    value,
                    postfixText(postfix),
                    root
            );

        } catch (IllegalArgumentException ex) {

            errors.add(
                    "Línea " + line +
                            ": " +
                            ex.getMessage()
            );

            return new Analysis(
                    "error",
                    "",
                    "",
                    null
            );
        }
    }

    private String resultType(
            String left,
            String right,
            String operator
    ) {

        if (operator.equals("+")) {

            if (left.equals("String")
                    || right.equals("String")) {

                return "String";
            }

            if (isNumeric(left)
                    && isNumeric(right)) {

                if (left.equals("double")
                        || right.equals("double")) {

                    return "double";
                }

                return "int";
            }
        }

        if (operator.equals("-")
                || operator.equals("*")
                || operator.equals("/")) {

            if (isNumeric(left)
                    && isNumeric(right)) {

                if (left.equals("double")
                        || right.equals("double")) {

                    return "double";
                }

                return "int";
            }
        }

        return "error";
    }

    private boolean isNumeric(String type) {

        return type.equals("int")
                || type.equals("double");
    }

    private List<Token> tokenize(String expression) {

        List<Token> result =
                new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "\\s*(\"[^\"]*\"|\\d+(?:\\.\\d+)?|true|false|[A-Za-z_]\\w*|[()+\\-*/])"
        );

        Matcher matcher =
                pattern.matcher(expression);

        int position = 0;

        while (matcher.find()) {

            if (matcher.start() != position) {

                String ignored =
                        expression.substring(
                                position,
                                matcher.start()
                        );

                if (!ignored.isBlank()) {

                    throw new IllegalArgumentException(
                            "Token inválido: " +
                                    ignored.trim()
                    );
                }
            }

            String text =
                    matcher.group(1);

            Kind kind;

            if (text.startsWith("\"")) {

                kind = Kind.STRING;

            } else if (text.matches(
                    "\\d+(?:\\.\\d+)?"
            )) {

                kind = Kind.NUMBER;

            } else if (text.equals("true")
                    || text.equals("false")) {

                kind = Kind.BOOL;

            } else if (text.matches(
                    "[A-Za-z_]\\w*"
            )) {

                kind = Kind.ID;

            } else if (text.equals("(")) {

                kind = Kind.LP;

            } else if (text.equals(")")) {

                kind = Kind.RP;

            } else {

                kind = Kind.OP;
            }

            result.add(
                    new Token(text, kind)
            );

            position = matcher.end();
        }

        if (position < expression.length()) {

            String remaining =
                    expression.substring(position);

            if (!remaining.isBlank()) {

                throw new IllegalArgumentException(
                        "Token inválido: " +
                                remaining.trim()
                );
            }
        }

        return result;
    }

    private int precedence(String operator) {

        if (operator.equals("+")
                || operator.equals("-")) {

            return 1;
        }

        return 2;
    }

    private List<Token> toPostfix(
            List<Token> input
    ) {

        List<Token> output =
                new ArrayList<>();

        Deque<Token> operators =
                new ArrayDeque<>();

        for (Token token : input) {

            if (token.kind() == Kind.NUMBER
                    || token.kind() == Kind.STRING
                    || token.kind() == Kind.BOOL
                    || token.kind() == Kind.ID) {

                output.add(token);

            } else if (token.kind() == Kind.LP) {

                operators.push(token);

            } else if (token.kind() == Kind.RP) {

                while (!operators.isEmpty()
                        && operators.peek().kind()
                        != Kind.LP) {

                    output.add(
                            operators.pop()
                    );
                }

                if (operators.isEmpty()) {

                    throw new IllegalArgumentException(
                            "Paréntesis desbalanceados."
                    );
                }

                operators.pop();

            } else if (token.kind() == Kind.OP) {

                while (!operators.isEmpty()
                        && operators.peek().kind()
                        == Kind.OP
                        && precedence(
                        operators.peek().text()
                ) >= precedence(
                        token.text()
                )) {

                    output.add(
                            operators.pop()
                    );
                }

                operators.push(token);
            }
        }

        while (!operators.isEmpty()) {

            if (operators.peek().kind()
                    == Kind.LP) {

                throw new IllegalArgumentException(
                        "Paréntesis desbalanceados."
                );
            }

            output.add(
                    operators.pop()
            );
        }

        return output;
    }

    private String postfixText(
            List<Token> postfix
    ) {

        return postfix.stream()
                .map(Token::text)
                .reduce(
                        (a, b) -> a + " " + b
                )
                .orElse("");
    }

    public String buildExpressionTree(
            String expression
    ) {

        try {

            List<Token> tokens =
                    tokenize(expression);

            List<Token> postfix =
                    toPostfix(tokens);

            Deque<Node> nodes =
                    new ArrayDeque<>();

            for (Token token : postfix) {

                if (token.kind() == Kind.NUMBER
                        || token.kind() == Kind.STRING
                        || token.kind() == Kind.BOOL
                        || token.kind() == Kind.ID) {

                    nodes.push(
                            new Node(
                                    token.text(),
                                    null,
                                    null
                            )
                    );

                } else if (token.kind()
                        == Kind.OP) {

                    if (nodes.size() < 2) {
                        return "Error: expresión incompleta.";
                    }

                    Node right =
                            nodes.pop();

                    Node left =
                            nodes.pop();

                    nodes.push(
                            new Node(
                                    token.text(),
                                    left,
                                    right
                            )
                    );
                }
            }

            if (nodes.size() != 1) {
                return "Error: expresión inválida.";
            }

            return tree(
                    nodes.pop(),
                    "",
                    true
            );

        } catch (Exception e) {

            return "Error: " + e.getMessage();
        }
    }

    private String tree(
            Node node,
            String prefix,
            boolean last
    ) {

        if (node == null) {
            return "";
        }

        StringBuilder result =
                new StringBuilder();

        result.append(prefix)
                .append(last ? "└── " : "├── ")
                .append(node.value)
                .append("\n");

        String childPrefix =
                prefix +
                        (last ? "    " : "│   ");

        if (node.left != null) {

            result.append(
                    tree(
                            node.left,
                            childPrefix,
                            node.right == null
                    )
            );
        }

        if (node.right != null) {

            result.append(
                    tree(
                            node.right,
                            childPrefix,
                            true
                    )
            );
        }

        return result.toString();
    }

    private String evaluate(
            String expression,
            String type
    ) {

        try {

            if (type.equals("String")) {

                String text =
                        expression.trim();

                // Cadena simple
                if (text.startsWith("\"")
                        && text.endsWith("\"")) {

                    return text.substring(
                            1,
                            text.length() - 1
                    );
                }

                // Concatenación de Strings
                List<Token> postfix =
                        toPostfix(
                                tokenize(expression)
                        );

                StringBuilder result =
                        new StringBuilder();

                for (Token token : postfix) {

                    if (token.kind()
                            == Kind.STRING) {

                        result.append(
                                token.text()
                                        .substring(
                                                1,
                                                token.text().length() - 1
                                        )
                        );

                    } else if (token.kind()
                            == Kind.ID) {

                        Symbol symbol =
                                symbols.get(
                                        token.text()
                                );

                        if (symbol != null) {
                            result.append(
                                    symbol.value()
                            );
                        }
                    }
                }

                return result.toString();
            }

            if (type.equals("boolean")) {

                return expression.trim();
            }

            double result =
                    evaluateNumeric(expression);

            if (type.equals("int")) {

                return String.valueOf(
                        (int) result
                );
            }

            return String.valueOf(result);

        } catch (Exception e) {

            return expression.trim();
        }
    }

    private double evaluateNumeric(
            String expression
    ) {

        List<Token> postfix =
                toPostfix(
                        tokenize(expression)
                );

        Deque<Double> values =
                new ArrayDeque<>();

        for (Token token : postfix) {

            if (token.kind()
                    == Kind.NUMBER) {

                values.push(
                        Double.parseDouble(
                                token.text()
                        )
                );

            } else if (token.kind()
                    == Kind.ID) {

                Symbol symbol =
                        symbols.get(
                                token.text()
                        );

                if (symbol == null
                        || !isNumeric(
                        symbol.type()
                )) {

                    throw new IllegalArgumentException();
                }

                values.push(
                        Double.parseDouble(
                                symbol.value()
                        )
                );

            } else if (token.kind()
                    == Kind.OP) {

                if (values.size() < 2) {
                    throw new IllegalArgumentException();
                }

                double right =
                        values.pop();

                double left =
                        values.pop();

                double result;

                switch (token.text()) {

                    case "+" ->
                            result = left + right;

                    case "-" ->
                            result = left - right;

                    case "*" ->
                            result = left * right;

                    case "/" -> {

                        if (right == 0) {
                            throw new ArithmeticException(
                                    "División entre cero."
                            );
                        }

                        result =
                                left / right;
                    }

                    default ->
                            throw new IllegalArgumentException();
                }

                values.push(result);
            }
        }

        if (values.size() != 1) {
            throw new IllegalArgumentException();
        }

        return values.pop();
    }

    public Map<String, Symbol> symbols() {
        return symbols;
    }

    public List<ExpressionRow> expressions() {
        return expressions;
    }

    public List<String> errors() {
        return errors;
    }

    public List<String> stack() {
        return stack;
    }

    public List<String> translation() {
        return translation;
    }
}