
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

public class LCOMCalculator {

    private static final String SOURCE_DIRECTORY = "src/main/java";
    private static final String EXCLUDED_PACKAGE = "metrics";

    private static final Pattern CLASS_PATTERN =
            Pattern.compile(
                    "\\b(?:class|interface|enum)\\s+([A-Za-z_$][A-Za-z0-9_$]*)"
            );

    private static final Pattern FIELD_PATTERN =
            Pattern.compile(
                    "(?m)^\\s*" +
                    "(?:(?:public|protected|private|static|final|transient|volatile)\\s+)*" +
                    "(?:[\\w<>?,\\[\\].]+\\s+)+" +
                    "([A-Za-z_$][A-Za-z0-9_$]*)\\s*(?:=[^;]*)?;"
            );

    private static final Pattern METHOD_PATTERN =
            Pattern.compile(
                    "(?m)^\\s*" +
                    "(?:(?:public|protected|private|static|final|synchronized|native|abstract)\\s+)*" +
                    "(?:[\\w<>?,\\[\\].]+\\s+)+" +
                    "([A-Za-z_$][A-Za-z0-9_$]*)\\s*" +
                    "\\([^;{}]*\\)\\s*(?:throws\\s+[^\\{]+)?\\{"
            );

    public static void main(String[] args) {

        Path sourcePath = Paths.get(SOURCE_DIRECTORY);

        try {
            List<Path> javaFiles;

            try (Stream<Path> paths = Files.walk(sourcePath)) {
                javaFiles = paths
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".java"))
                        .filter(path -> !path.toString().contains(
                                java.io.File.separator
                                        + EXCLUDED_PACKAGE
                                        + java.io.File.separator))
                        .collect(Collectors.toList());
            }

            if (javaFiles.isEmpty()) {
                System.out.println("No Java source files found.");
                return;
            }

            System.out.println();
            System.out.println("==============================================================");
            System.out.println("       LCOM - LACK OF COHESION IN METHODS");
            System.out.println("==============================================================");
            System.out.printf("%-35s %s%n", "Class", "LCOM4");
            System.out.println("--------------------------------------------------------------");

            int totalLCOM = 0;
            int totalClasses = 0;

            for (Path file : javaFiles) {
                String source = Files.readString(file);

                Matcher classMatcher = CLASS_PATTERN.matcher(source);
                if (!classMatcher.find()) {
                    continue;
                }

                String className = classMatcher.group(1);

                Set<String> fields = new LinkedHashSet<>();
                Matcher fieldMatcher = FIELD_PATTERN.matcher(source);

                while (fieldMatcher.find()) {
                    fields.add(fieldMatcher.group(1));
                }

                Map<String, Set<String>> methodFields = new LinkedHashMap<>();
                Matcher methodMatcher = METHOD_PATTERN.matcher(source);

                while (methodMatcher.find()) {
                    String methodName = methodMatcher.group(1);

                    // Ignore constructors and control structures.
                    if (methodName.equals(className)
                            || methodName.equals("if")
                            || methodName.equals("for")
                            || methodName.equals("while")
                            || methodName.equals("switch")
                            || methodName.equals("catch")
                            || methodName.equals("try")
                            || methodName.equals("return")) {
                        continue;
                    }

                    int bodyStart = methodMatcher.end() - 1;
                    int bodyEnd = findMatchingBrace(source, bodyStart);

                    if (bodyEnd < 0) {
                        continue;
                    }

                    String body = source.substring(bodyStart + 1, bodyEnd);
                    Set<String> usedFields = new LinkedHashSet<>();

                    for (String field : fields) {
                        Pattern usagePattern = Pattern.compile(
                                "\\b" + Pattern.quote(field) + "\\b"
                        );

                        if (usagePattern.matcher(body).find()) {
                            usedFields.add(field);
                        }
                    }

                    methodFields.put(methodName, usedFields);
                }

                int lcom4 = calculateLCOM4(methodFields);
                totalLCOM += lcom4;
                totalClasses++;

                System.out.printf(
                        "%-35s %d%n",
                        className,
                        lcom4
                );
            }

            double averageLCOM = totalClasses == 0
                    ? 0.0
                    : (double) totalLCOM / totalClasses;

            System.out.println("--------------------------------------------------------------");
            System.out.println("Total Classes : " + totalClasses);
            System.out.println("Total LCOM    : " + totalLCOM);
            System.out.printf("Average LCOM  : %.2f%n", averageLCOM);
            System.out.println("==============================================================");

        } catch (IOException e) {
            System.err.println("Error reading Java source files.");
            e.printStackTrace();
        }
    }

    private static int findMatchingBrace(String source, int openBrace) {
        int depth = 0;
        boolean inString = false;
        boolean inChar = false;
        boolean escaped = false;

        for (int i = openBrace; i < source.length(); i++) {
            char c = source.charAt(i);

            if (escaped) {
                escaped = false;
                continue;
            }

            if ((inString || inChar) && c == '\\') {
                escaped = true;
                continue;
            }

            if (!inChar && c == '"') {
                inString = !inString;
                continue;
            }

            if (!inString && c == '\'') {
                inChar = !inChar;
                continue;
            }

            if (inString || inChar) {
                continue;
            }

            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }

        return -1;
    }

    private static int calculateLCOM4(
            Map<String, Set<String>> methodFields) {

        if (methodFields.isEmpty()) {
            return 0;
        }

        List<String> methods = new ArrayList<>(methodFields.keySet());
        int n = methods.size();

        int[] parent = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {

                Set<String> first = methodFields.get(methods.get(i));
                Set<String> second = methodFields.get(methods.get(j));

                Set<String> common = new HashSet<>(first);
                common.retainAll(second);

                if (!common.isEmpty()) {
                    union(parent, i, j);
                }
            }
        }

        Set<Integer> components = new HashSet<>();
        for (int i = 0; i < n; i++) {
            components.add(find(parent, i));
        }

        return components.size();
    }

    private static int find(int[] parent, int x) {
        while (parent[x] != x) {
            parent[x] = parent[parent[x]];
            x = parent[x];
        }
        return x;
    }

    private static void union(int[] parent, int x, int y) {
        int rootX = find(parent, x);
        int rootY = find(parent, y);

        if (rootX != rootY) {
            parent[rootY] = rootX;
        }
    }
}
