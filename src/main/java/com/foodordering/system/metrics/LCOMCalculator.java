
package com.foodordering.system.metrics;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.tools.JavaCompiler;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

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
                    "(?:private|protected|public)\\s+" +
                    "(?!static\\b)" +
                    "(?:final\\s+)?" +
                    "[A-Za-z_$][A-Za-z0-9_$]*(?:\\s*<[^;{}]*>)?(?:\\[\\])*\\s+" +
                    "([A-Za-z_$][A-Za-z0-9_$]*)\\s*(?:=|;)"
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
                                java.io.File.separator +
                                EXCLUDED_PACKAGE +
                                java.io.File.separator))
                        .collect(Collectors.toList());
            }

            if (javaFiles.isEmpty()) {
                System.out.println("No Java source files found.");
                return;
            }

            Map<String, Integer> lcomResults = new TreeMap<>();

            for (Path file : javaFiles) {
                String source = Files.readString(file);

                Matcher classMatcher = CLASS_PATTERN.matcher(source);

                if (!classMatcher.find()) {
                    continue;
                }

                String className = classMatcher.group(1);
                int lcom = calculateLCOM4(source);

                lcomResults.put(className, lcom);
            }

            System.out.println();
            System.out.println(
                    "==============================================================");
            System.out.println(
                    "       LCOM - LACK OF COHESION IN METHODS");
            System.out.println(
                    "==============================================================");

            System.out.printf("%-35s %s%n", "Class", "LCOM4");

            System.out.println(
                    "--------------------------------------------------------------");

            int totalLCOM = 0;

            for (Map.Entry<String, Integer> entry :
                    lcomResults.entrySet()) {

                System.out.printf(
                        "%-35s %d%n",
                        entry.getKey(),
                        entry.getValue());

                totalLCOM += entry.getValue();
            }

            int totalClasses = lcomResults.size();

            double averageLCOM = totalClasses == 0
                    ? 0
                    : (double) totalLCOM / totalClasses;

            System.out.println(
                    "--------------------------------------------------------------");
            System.out.println("Total Classes : " + totalClasses);
            System.out.println("Total LCOM    : " + totalLCOM);
            System.out.printf("Average LCOM  : %.2f%n", averageLCOM);
            System.out.println(
                    "==============================================================");

        } catch (IOException e) {
    System.err.println(
            "Error reading Java source files: " + e.getMessage()
    );
}
    }

    /*
     * LCOM4 calculation:
     * Methods accessing a common instance field are connected.
     * LCOM4 equals the number of connected components.
     */
    private static int calculateLCOM4(String source) {

        Set<String> fields = new HashSet<>();
        Matcher fieldMatcher = FIELD_PATTERN.matcher(source);

        while (fieldMatcher.find()) {
            fields.add(fieldMatcher.group(1));
        }

        if (fields.isEmpty()) {
            return 0;
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        if (compiler == null) {
            throw new IllegalStateException(
                    "A JDK is required to calculate LCOM4.");
        }

        Map<String, Set<String>> methodFields = new LinkedHashMap<>();

        SimpleJavaFileObject fileObject =
                new SimpleJavaFileObject(
                        URI.create("string:///Source.java"),
                        SimpleJavaFileObject.Kind.SOURCE) {

                    @Override
                    public CharSequence getCharContent(
                            boolean ignoreEncodingErrors) {
                        return source;
                    }
                };

        try {
            JavacTask task = (JavacTask) compiler.getTask(
                    null,
                    null,
                    null,
                    Arrays.asList("-proc:none"),
                    null,
                    Collections.singletonList(fileObject));

            Iterator<? extends CompilationUnitTree> units =
                    task.parse().iterator();

            if (!units.hasNext()) {
                return 0;
            }

            CompilationUnitTree unit = units.next();
            Trees trees = Trees.instance(task);
            var positions = trees.getSourcePositions();

            new TreePathScanner<Void, Void>() {

                @Override
                public Void visitMethod(MethodTree method, Void unused) {

                    if (method.getBody() == null) {
                        return super.visitMethod(method, unused);
                    }

                    long start = positions.getStartPosition(
                            unit, method.getBody());

                    long end = positions.getEndPosition(
                            unit, method.getBody());

                    if (start < 0 || end <= start
                            || end > source.length()) {
                        return super.visitMethod(method, unused);
                    }

                    String body = source.substring(
                            (int) start, (int) end);

                    Set<String> accessedFields = new HashSet<>();

                    for (String field : fields) {
                        Pattern reference = Pattern.compile(
                                "\\b" + Pattern.quote(field) + "\\b");

                        if (reference.matcher(body).find()) {
                            accessedFields.add(field);
                        }
                    }

                    if (!accessedFields.isEmpty()) {
                        String name = method.getName().toString();
                        String uniqueName = name;
                        int counter = 2;

                        while (methodFields.containsKey(uniqueName)) {
                            uniqueName = name + "#" + counter++;
                        }

                        methodFields.put(uniqueName, accessedFields);
                    }

                    return super.visitMethod(method, unused);
                }
            }.scan(unit, null);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unable to parse source for LCOM4.", e);
        }

        if (methodFields.isEmpty()) {
            return 0;
        }

        List<String> methods =
                new ArrayList<>(methodFields.keySet());

        Map<String, Set<String>> graph = new HashMap<>();

        for (String method : methods) {
            graph.put(method, new HashSet<>());
        }

        for (int i = 0; i < methods.size(); i++) {
            for (int j = i + 1; j < methods.size(); j++) {

                String methodA = methods.get(i);
                String methodB = methods.get(j);

                Set<String> commonFields =
                        new HashSet<>(methodFields.get(methodA));

                commonFields.retainAll(methodFields.get(methodB));

                if (!commonFields.isEmpty()) {
                    graph.get(methodA).add(methodB);
                    graph.get(methodB).add(methodA);
                }
            }
        }

        Set<String> visited = new HashSet<>();
        int components = 0;

        for (String method : methods) {
            if (visited.contains(method)) {
                continue;
            }

            components++;
            explore(method, graph, visited);
        }

        return components;
    }

    /*
     * Depth-first search for connected components.
     */
    private static void explore(
            String method,
            Map<String, Set<String>> graph,
            Set<String> visited) {

        visited.add(method);

        for (String neighbour : graph.get(method)) {
            if (!visited.contains(neighbour)) {
                explore(neighbour, graph, visited);
            }
        }
    }
}
