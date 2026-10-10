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

public class CBOCalculator {

    /*
     * Source directory of the actual application.
     */
    private static final String SOURCE_DIRECTORY =
            "src/main/java";

    /*
     * Package containing metric-calculation utilities.
     * This package must NOT be included in the CBO calculation.
     */
    private static final String EXCLUDED_PACKAGE =
            "metrics";

    /*
     * Detects Java class, interface and enum declarations.
     */
    private static final Pattern CLASS_PATTERN =
            Pattern.compile(
                    "\\b(?:class|interface|enum)\\s+([A-Za-z_$][A-Za-z0-9_$]*)"
            );

    public static void main(String[] args) {

        Path sourcePath = Paths.get(SOURCE_DIRECTORY);

        try {

            // =========================================================
            // STEP 1: Find application Java source files
            // =========================================================

            List<Path> javaFiles;

            try (Stream<Path> paths = Files.walk(sourcePath)) {

                javaFiles = paths
                        .filter(Files::isRegularFile)
                        .filter(path ->
                                path.toString().endsWith(".java"))
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
            // STEP 2: Find application classes
            // =========================================================

            Set<String> projectClasses =
                    new TreeSet<>();

            for (Path file : javaFiles) {

                String source =
                        Files.readString(file);

                Matcher matcher =
                        CLASS_PATTERN.matcher(source);

                while (matcher.find()) {

                    projectClasses.add(
                            matcher.group(1)
                    );
                }
            }

            // =========================================================
            // STEP 3: Calculate CBO
            // =========================================================

            Map<String, Set<String>> couplingMap =
                    new TreeMap<>();

            for (Path file : javaFiles) {

                String source =
                        Files.readString(file);

                Matcher classMatcher =
                        CLASS_PATTERN.matcher(source);

                if (!classMatcher.find()) {
                    continue;
                }

                String currentClass =
                        classMatcher.group(1);

                Set<String> coupledClasses =
                        new TreeSet<>();

                /*
                 * Check references to other application classes.
                 */
                for (String projectClass :
                        projectClasses) {

                    // A class is not coupled to itself.
                    if (projectClass.equals(currentClass)) {
                        continue;
                    }

                    Pattern referencePattern =
                            Pattern.compile(
                                    "\\b"
                                            + Pattern.quote(
                                                    projectClass)
                                            + "\\b"
                            );

                    Matcher referenceMatcher =
                            referencePattern.matcher(source);

                    if (referenceMatcher.find()) {

                        coupledClasses.add(
                                projectClass
                        );
                    }
                }

                couplingMap.put(
                        currentClass,
                        coupledClasses
                );
            }

            // =========================================================
            // STEP 4: Display results
            // =========================================================

            System.out.println();
            System.out.println(
                    "=============================================================="
            );
            System.out.println(
                    "          CBO - COUPLING BETWEEN OBJECTS"
            );
            System.out.println(
                    "=============================================================="
            );

            System.out.printf(
                    "%-30s %-5s %s%n",
                    "Class",
                    "CBO",
                    "Coupled Application Classes"
            );

            System.out.println(
                    "--------------------------------------------------------------"
            );

            int totalCBO = 0;

            for (Map.Entry<String, Set<String>> entry :
                    couplingMap.entrySet()) {

                String className =
                        entry.getKey();

                Set<String> coupledClasses =
                        entry.getValue();

                int cbo =
                        coupledClasses.size();

                totalCBO += cbo;

                String coupled =
                        coupledClasses.isEmpty()
                                ? "-"
                                : String.join(
                                        ", ",
                                        coupledClasses
                                );

                System.out.printf(
                        "%-30s %-5d %s%n",
                        className,
                        cbo,
                        coupled
                );
            }

            // =========================================================
            // STEP 5: Calculate average CBO
            // =========================================================

            int totalClasses =
                    couplingMap.size();

            double averageCBO =
                    totalClasses == 0
                            ? 0
                            : (double) totalCBO
                            / totalClasses;

            System.out.println(
                    "--------------------------------------------------------------"
            );

            System.out.println(
                    "Total Classes : "
                            + totalClasses
            );

            System.out.println(
                    "Total CBO     : "
                            + totalCBO
            );

            System.out.printf(
                    "Average CBO   : %.2f%n",
                    averageCBO
            );

            System.out.println(
                    "=============================================================="
            );

        } catch (IOException e) {

            System.err.println(
                    "Error reading Java source files."
            );

            e.printStackTrace();
        }
    }
}