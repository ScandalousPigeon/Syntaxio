package com.example.syntaxio.runner;

import com.example.syntaxio.model.TestCase;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CodeExecutor {

    private static final String CLASS_NAME = "UserSolution";
    private static final String METHOD_NAME = "runSolution";
    private static final Pattern PUBLIC_METHOD_PATTERN = Pattern.compile(
            "public\\s+(?:static\\s+)?[\\w<>\\[\\]]+\\s+([A-Za-z_$][\\w$]*)\\s*\\("
    );

    public static List<TestCase> executeTests(String userCode, List<TestCase> testCases) {
        String userMethodName;

        try {
            userMethodName = extractUserMethodName(userCode);
        } catch (IllegalArgumentException e) {
            markAllTestsFailed(testCases, e.getMessage());
            return testCases;
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            markAllTestsFailed(testCases, "Execution Error: Java compiler is not available.");
            return testCases;
        }

        for (TestCase testCase : testCases) {
            executeSingleTest(compiler, userCode, userMethodName, testCase);
        }

        return testCases;
    }

    private static void executeSingleTest(
            JavaCompiler compiler,
            String userCode,
            String userMethodName,
            TestCase testCase
    ) {
        String fullCode = wrapCodeForTesting(userCode, userMethodName, testCase.getInput());

        try {
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            MemoryJavaFileManager fileManager = new MemoryJavaFileManager(compiler.getStandardFileManager(null, null, null));

            JavaFileObject file = new MemoryJavaFileObject(CLASS_NAME, fullCode);
            CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, List.of(file));

            if (!task.call()) {
                String errors = diagnostics.getDiagnostics().stream()
                    .map(d -> d.getMessage(null))
                    .collect(Collectors.joining("\n"));
                markTestFailed(testCase, "Compilation Error: " + errors);
                return;
            }

            // Load the compiled class directly from in-memory bytecode
            byte[] bytecode = fileManager.getBytecode();
            ClassLoader classLoader = new ClassLoader(CodeExecutor.class.getClassLoader()) {
                @Override
                protected Class<?> findClass(String name) throws ClassNotFoundException {
                    return defineClass(name, bytecode, 0, bytecode.length);
                }
            };
            Class<?> clazz = classLoader.loadClass(CLASS_NAME);
            Method method = clazz.getMethod(METHOD_NAME);
            Object instance = clazz.getDeclaredConstructor().newInstance();

            try {
                Object result = method.invoke(instance);
                String actualOutput = stringifyResult(result);
                testCase.setActualOutput(actualOutput);
                testCase.setPassed(actualOutput.equals(testCase.getExpectedOutput()));
            } catch (Exception e) {
                Throwable cause = e.getCause();
                markTestFailed(testCase, "Runtime Error: " + (cause != null ? cause.getMessage() : e.getMessage()));
            }
        } catch (Exception e) {
            markTestFailed(testCase, "Execution Error: " + e.getMessage());
        }
    }

    private static String wrapCodeForTesting(String userCode, String userMethodName, String input) {
        String arguments = input == null ? "" : input.trim();
        return """
            import java.util.*;

            public class UserSolution {
                %s

                public Object runSolution() {
                    return %s(%s);
                }
            }
            """.formatted(userCode, userMethodName, arguments);
    }

    private static String extractUserMethodName(String userCode) {
        Matcher matcher = PUBLIC_METHOD_PATTERN.matcher(userCode);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Compilation Error: Could not find a public method to test.");
        }

        return matcher.group(1);
    }

    private static String stringifyResult(Object result) {
        if (result == null) {
            return "null";
        }

        Class<?> resultClass = result.getClass();
        if (!resultClass.isArray()) {
            return String.valueOf(result);
        }

        if (result instanceof int[] array) return Arrays.toString(array);
        if (result instanceof long[] array) return Arrays.toString(array);
        if (result instanceof double[] array) return Arrays.toString(array);
        if (result instanceof boolean[] array) return Arrays.toString(array);
        if (result instanceof char[] array) return Arrays.toString(array);
        if (result instanceof byte[] array) return Arrays.toString(array);
        if (result instanceof short[] array) return Arrays.toString(array);
        if (result instanceof float[] array) return Arrays.toString(array);

        return Arrays.deepToString((Object[]) result);
    }

    private static void markAllTestsFailed(List<TestCase> testCases, String errorMessage) {
        for (TestCase testCase : testCases) {
            markTestFailed(testCase, errorMessage);
        }
    }

    private static void markTestFailed(TestCase testCase, String errorMessage) {
        testCase.setPassed(false);
        testCase.setActualOutput(errorMessage);
    }

    static class MemoryJavaFileManager extends ForwardingJavaFileManager<JavaFileManager> {
        private MemoryJavaFileObject outputFile;

        public MemoryJavaFileManager(JavaFileManager fileManager) {
            super(fileManager);
        }

        public byte[] getBytecode() {
            return outputFile.getBytecode();
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className,
                                                   JavaFileObject.Kind kind, FileObject sibling) {
            outputFile = new MemoryJavaFileObject(className, kind);
            return outputFile;
        }
    }

    static class MemoryJavaFileObject extends SimpleJavaFileObject {
        private String code;
        private ByteArrayOutputStream bytecode;

        public MemoryJavaFileObject(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        public MemoryJavaFileObject(String name, Kind kind) {
            super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }

        @Override
        public OutputStream openOutputStream() {
            bytecode = new ByteArrayOutputStream();
            return bytecode;
        }

        public byte[] getBytecode() {
            return bytecode.toByteArray();
        }
    }
}
