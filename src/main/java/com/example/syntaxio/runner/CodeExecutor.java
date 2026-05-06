package com.example.syntaxio.runner;

import com.example.syntaxio.model.TestCase;

import javax.tools.*;
import javax.tools.JavaCompiler.CompilationTask;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

public class CodeExecutor {

    private static final String CLASS_NAME = "UserSolution";
    private static final String METHOD_NAME = "runSolution";

    public static List<TestCase> executeTests(String userCode, List<TestCase> testCases) {
        String fullCode = wrapCodeForTesting(userCode);

        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            MemoryJavaFileManager fileManager = new MemoryJavaFileManager(compiler.getStandardFileManager(null, null, null));

            JavaFileObject file = new MemoryJavaFileObject(CLASS_NAME, fullCode);
            CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, List.of(file));

            if (!task.call()) {
                String errors = diagnostics.getDiagnostics().stream()
                    .map(d -> d.getMessage(null))
                    .collect(Collectors.joining("\n"));
                markAllTestsFailed(testCases, "Compilation Error: " + errors);
                return testCases;
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

            for (TestCase testCase : testCases) {
                try {
                    Object result = method.invoke(instance);
                    testCase.setActualOutput(String.valueOf(result));
                    testCase.setPassed(String.valueOf(result).equals(testCase.getExpectedOutput()));
                } catch (Exception e) {
                    testCase.setPassed(false);
                    Throwable cause = e.getCause();
                    testCase.setActualOutput("Runtime Error: " + (cause != null ? cause.getMessage() : e.getMessage()));
                }
            }

        } catch (Exception e) {
            markAllTestsFailed(testCases, "Execution Error: " + e.getMessage());
        }

        return testCases;
    }

    private static String wrapCodeForTesting(String userCode) {
        return """
            import java.util.*;

            public class UserSolution {
                %s

                public int runSolution() {
                    // This will call your method with test inputs
                    // Simplified - you'll need to adapt based on challenge
                    return 0;
                }
            }
            """.formatted(userCode);
    }

    private static void markAllTestsFailed(List<TestCase> testCases, String errorMessage) {
        for (TestCase testCase : testCases) {
            testCase.setPassed(false);
            testCase.setActualOutput(errorMessage);
        }
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
