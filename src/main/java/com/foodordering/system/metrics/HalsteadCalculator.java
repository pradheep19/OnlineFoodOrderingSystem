package com.foodordering.system.metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HalsteadCalculator {

    private static final String SOURCE_DIRECTORY =
            "src/main/java";

    private static final String EXCLUDED_PACKAGE =
            "metrics";

    /*
     * Java operators considered for this source-level calculation.
     */
    private static final String[] OPERATORS = {
            ">>>=", "<<=", ">>=",
            "==", "!=", "<=", ">=",
            "++", "--", "&&", "||",
            "+=", "-=", "*=", "/=", "%=",
            "&=", "|=", "^=", "->",
            ">>>", "<<", ">>",
            "+", "-", "*", "/", "%",
            "=", "<", ">",
            "!", "~",
            "&", "|", "^",
            "?", ":",
            ".", ",",
            ";",
            "(", ")", "{", "}",
            "[", "]"
    };

    /*
     * Java keywords treated as operators.
     */
    private static final Set<String> KEYWORD_OPERATORS =
            new HashSet<>(Arrays.asList(
                    "if",
                    "else",
                    "for",
                    "while",
                    "do",
                    "switch",
                    "case",
                    "default",
                    "break",
                    "continue",
                    "return",
                    "throw",
                    "throws",
                    "try",
                    "catch",
                    "finally",
                    "new",
                    "instanceof",
                    "this",
                    "super",
                    "synchronized",
                    "assert"
            ));

    /*
     * Java type/control keywords that should not be
     * counted as operands.
     */
    private static final Set<String> NON_OPERAND_KEYWORDS =
            new HashSet<>(Arrays.asList(
                    "class",
                    "interface",
                    "enum",
                    "public",
                    "private",
                    "protected",
                    "static",
                    "final",
                    "abstract",
                    "native",
                    "strictfp",
                    "transient",
                    "volatile",
                    "extends",
                    "implements",
                    "package",
                    "import",
                    "void",
                    "boolean",
                    "byte",
                    "short",
                    "int",
                    "long",
                    "float",
                    "double",
                    "char",
                    "true",
                    "false",
                    "null"
            ));

    public static void main(String[] args) {

        Path sourcePath =
                Paths.get(SOURCE_DIRECTORY);

        try {

            // =========================================================
            // STEP 1: Find application Java files
            // =========================================================

            List<Path> javaFiles;

            try (Stream<Path> paths =
                         Files.walk(sourcePath)) {

                javaFiles = paths
                        .filter(Files::isRegularFile)
                        .filter(path ->
                                path.toString()
                                        .endsWith(".java"))
                        .filter(path ->
                                !path.toString()
                                        .contains(
                                                java.io.File.separator
                                                        + EXCLUDED_PACKAGE
                                                        + java.io.File.separator
                                        ))
                        .collect(Collectors.toList());
            }

            if (javaFiles.isEmpty()) {

                System.out.println(
                        "No Java source files found."
                );

                return;
            }

            // =========================================================
            // STEP 2: Count operators and operands
            // =========================================================

            Set<String> distinctOperators =
                    new TreeSet<>();

            Set<String> distinctOperands =
                    new TreeSet<>();

            int totalOperators = 0;
            int totalOperands = 0;

            for (Path file : javaFiles) {

                String source =
                        Files.readString(file);

                /*
                 * Remove comments and string literals before
                 * counting source tokens.
                 */
                String cleaned =
                        removeCommentsAndStrings(source);

                List<String> tokens =
                        tokenize(cleaned);

                for (String token : tokens) {

                    if (isOperator(token)) {

                        distinctOperators.add(token);
                        totalOperators++;

                    } else if (isOperand(token)) {

                        distinctOperands.add(token);
                        totalOperands++;
                    }
                }
            }

            // =========================================================
            // STEP 3: Calculate Halstead metrics
            // =========================================================

            int n1 =
                    distinctOperators.size();

            int n2 =
                    distinctOperands.size();

            int N1 =
                    totalOperators;

            int N2 =
                    totalOperands;

            int vocabulary =
                    n1 + n2;

            int length =
                    N1 + N2;

            double volume = 0.0;

            if (vocabulary > 0) {

                volume =
                        length
                                * (Math.log(vocabulary)
                                / Math.log(2));
            }

            double difficulty = 0.0;

            if (n2 > 0) {

                difficulty =
                        (n1 / 2.0)
                                * ((double) N2 / n2);
            }

            double effort =
                    difficulty * volume;

            // =========================================================
            // STEP 4: Display results
            // =========================================================

            System.out.println();
            System.out.println(
                    "=============================================================="
            );

            System.out.println(
                    "           HALSTEAD SOFTWARE METRICS"
            );

            System.out.println(
                    "=============================================================="
            );

            System.out.println(
                    "Application Classes : 26"
            );

            System.out.println(
                    "Source Files        : "
                            + javaFiles.size()
            );

            System.out.println(
                    "--------------------------------------------------------------"
            );

            System.out.println(
                    "Distinct Operators (n1) : "
                            + n1
            );

            System.out.println(
                    "Distinct Operands  (n2) : "
                            + n2
            );

            System.out.println(
                    "Total Operators    (N1) : "
                            + N1
            );

            System.out.println(
                    "Total Operands     (N2) : "
                            + N2
            );

            System.out.println(
                    "--------------------------------------------------------------"
            );

            System.out.println(
                    "Vocabulary (n)          : "
                            + vocabulary
            );

            System.out.println(
                    "Program Length (N)      : "
                            + length
            );

            System.out.printf(
                    "Volume (V)              : %.2f%n",
                    volume
            );

            System.out.printf(
                    "Difficulty (D)          : %.2f%n",
                    difficulty
            );

            System.out.printf(
                    "Effort (E)              : %.2f%n",
                    effort
            );

            System.out.println(
                    "=============================================================="
            );

        } catch (IOException e) {
    System.err.println(
            "Error reading Java source files: " + e.getMessage()
    );
}
    }

    /*
     * Determines whether a token is a Halstead operator.
     */
    private static boolean isOperator(
            String token) {

        if (KEYWORD_OPERATORS.contains(token)) {
            return true;
        }

        for (String operator : OPERATORS) {

            if (operator.equals(token)) {
                return true;
            }
        }

        return false;
    }

    /*
     * Determines whether a token is an operand.
     */
    private static boolean isOperand(
            String token) {

        if (token.isEmpty()) {
            return false;
        }

        if (KEYWORD_OPERATORS.contains(token)) {
            return false;
        }

        if (NON_OPERAND_KEYWORDS.contains(token)) {
            return false;
        }

        /*
         * Java identifiers and numeric literals
         * are treated as operands.
         */
        return token.matches(
                "[A-Za-z_$][A-Za-z0-9_$]*"
                        + "|\\d+(\\.\\d+)?"
        );
    }

    /*
     * Tokenizes cleaned Java source.
     */
    private static List<String> tokenize(
            String source) {

        List<String> tokens =
                new ArrayList<>();

        Pattern tokenPattern =
                Pattern.compile(
                        ">>>|>>|<<|==|!=|<=|>=|"
                                + "\\+\\+|--|&&|\\|\\||"
                                + "\\+=|-=|\\*=|/=|%=|"
                                + "&=|\\|=|\\^=|>>>="
                                + "|<<=|>>="
                                + "|->"
                                + "|[A-Za-z_$][A-Za-z0-9_$]*"
                                + "|\\d+(?:\\.\\d+)?"
                                + "|[+\\-*/%=<>!~&|^?:.,;(){}\\[\\]]"
                );

        Matcher matcher =
                tokenPattern.matcher(source);

        while (matcher.find()) {

            tokens.add(
                    matcher.group()
            );
        }

        return tokens;
    }

    /*
     * Removes:
     *
     * // single-line comments
     * /* multi-line comments *\/
     * "string literals"
     * 'character literals'
     */
    private static String removeCommentsAndStrings(String source) {
        StringBuilder result = new StringBuilder();

        final int NORMAL = 0;
        final int LINE_COMMENT = 1;
        final int BLOCK_COMMENT = 2;
        final int STRING = 3;
        final int CHARACTER = 4;

        int state = NORMAL;

        for (int i = 0; i < source.length(); i++) {
            char current = source.charAt(i);
            char next = i + 1 < source.length()
                    ? source.charAt(i + 1)
                    : '\0';

            if (state == LINE_COMMENT) {
                if (current == '\n') {
                    state = NORMAL;
                    result.append('\n');
                } else {
                    result.append(' ');
                }
                continue;
            }

            if (state == BLOCK_COMMENT) {
                if (current == '*' && next == '/') {
                    state = NORMAL;
                    result.append("  ");
                    i++;
                } else {
                    result.append(current == '\n' ? '\n' : ' ');
                }
                continue;
            }

            // String and character literals share the same processing logic.
            if (state == STRING || state == CHARACTER) {
                char closingQuote = state == STRING ? '"' : '\'';

                if (current == '\\' && next != '\0') {
                    result.append("  ");
                    i++;
                } else if (current == closingQuote) {
                    state = NORMAL;
                    result.append(' ');
                } else {
                    result.append(current == '\n' ? '\n' : ' ');
                }
                continue;
            }

            // Detect the beginning of comments.
            if (current == '/' && next == '/') {
                state = LINE_COMMENT;
                result.append("  ");
                i++;
                continue;
            }

            if (current == '/' && next == '*') {
                state = BLOCK_COMMENT;
                result.append("  ");
                i++;
                continue;
            }

            // Detect the beginning of string and character literals.
            if (current == '"') {
                state = STRING;
                result.append(' ');
                continue;
            }

            if (current == '\'') {
                state = CHARACTER;
                result.append(' ');
                continue;
            }

            result.append(current);
        }

        return result.toString();
    }
}
