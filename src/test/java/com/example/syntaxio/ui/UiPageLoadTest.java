package com.example.syntaxio.ui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UiPageLoadTest {

    private static final Pattern CONTROLLER_PATTERN = Pattern.compile("fx:controller=\"([^\"]+)\"");
    private static final Pattern EVENT_HANDLER_PATTERN = Pattern.compile("\\bon[A-Za-z]+=\"#([^\"]+)\"");
    private static final Pattern RESOURCE_REFERENCE_PATTERN = Pattern.compile(
            "(?:stylesheets|url|value)=\"@([^\"]+)\""
    );

    private static final List<String> UI_PAGES = List.of(
            "/com/example/syntaxio/login-screen.fxml",
            "/com/example/syntaxio/sign-up.fxml",
            "/com/example/syntaxio/main-menu.fxml",
            "/com/example/syntaxio/dashboard.fxml",
            "/com/example/syntaxio/coding-challenge-browser.fxml",
            "/com/example/syntaxio/coding-challenge.fxml"
    );

    @Test
    void allApplicationPagesHaveLoadableFxmlContracts() {
        assertAll(UI_PAGES.stream().map(page -> () -> assertPageLoadContract(page)));
    }

    private static void assertPageLoadContract(String fxmlPath) throws Exception {
        String fxml = readResource(fxmlPath);
        Class<?> controllerClass = assertControllerExists(fxmlPath, fxml);

        assertAll(
                () -> assertReferencedResourcesExist(fxmlPath, fxml),
                () -> assertEventHandlersExist(fxmlPath, fxml, controllerClass)
        );
    }

    private static Class<?> assertControllerExists(String fxmlPath, String fxml) throws ClassNotFoundException {
        Matcher matcher = CONTROLLER_PATTERN.matcher(fxml);
        assertTrue(matcher.find(), "Missing fx:controller in " + fxmlPath);

        return Class.forName(matcher.group(1));
    }

    private static void assertReferencedResourcesExist(String fxmlPath, String fxml) {
        Set<String> missingResources = new HashSet<>();
        Matcher matcher = RESOURCE_REFERENCE_PATTERN.matcher(fxml);

        while (matcher.find()) {
            String resolvedPath = resolveRelativeResource(fxmlPath, matcher.group(1));
            if (SyntaxioApp.class.getResource(resolvedPath) == null) {
                missingResources.add(resolvedPath);
            }
        }

        assertTrue(missingResources.isEmpty(), "Missing resource(s) referenced by "
                + fxmlPath + ": " + missingResources);
    }

    private static void assertEventHandlersExist(
            String fxmlPath,
            String fxml,
            Class<?> controllerClass
    ) {
        Set<String> missingHandlers = new HashSet<>();
        Matcher matcher = EVENT_HANDLER_PATTERN.matcher(fxml);

        while (matcher.find()) {
            String handlerName = matcher.group(1);
            if (!hasMethodNamed(controllerClass, handlerName)) {
                missingHandlers.add(handlerName);
            }
        }

        assertTrue(missingHandlers.isEmpty(), "Missing event handler(s) in "
                + controllerClass.getName() + " for " + fxmlPath + ": " + missingHandlers);
    }

    private static boolean hasMethodNamed(Class<?> controllerClass, String handlerName) {
        Class<?> currentClass = controllerClass;
        while (currentClass != null) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (!method.isSynthetic()
                        && !Modifier.isStatic(method.getModifiers())
                        && method.getName().equals(handlerName)) {
                    return true;
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return false;
    }

    private static String resolveRelativeResource(String fxmlPath, String relativeResource) {
        assertFalse(relativeResource.isBlank(), "Blank resource reference in " + fxmlPath);

        int lastSlash = fxmlPath.lastIndexOf('/');
        String basePath = lastSlash == -1 ? "/" : fxmlPath.substring(0, lastSlash + 1);
        return basePath + relativeResource;
    }

    private static String readResource(String path) throws IOException {
        try (InputStream stream = SyntaxioApp.class.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing FXML resource: " + path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
