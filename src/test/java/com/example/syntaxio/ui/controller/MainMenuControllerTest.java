package com.example.syntaxio.ui.controller;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.layout.GridPane;

class MainMenuControllerTest {

    @Test
    void constructorDoesNotFailWhenRuntimeDependenciesAreResolved() {
        assertDoesNotThrow(MainMenuController::new);
    }

    @Test
    void mainMenuFXMLKeepsChatControlsWiredToController() throws IOException {
        String fxml = readResource("/com/example/syntaxio/main-menu.fxml");

        assertAll(
                () -> assertTrue(fxml.contains("fx:id=\"chatScrollPane\"")),
                () -> assertTrue(fxml.contains("fx:id=\"chatContent\"")),
                () -> assertTrue(fxml.contains("fx:id=\"chatInput\"")),
                () -> assertTrue(fxml.contains("fx:id=\"sendButton\"")),
                () -> assertTrue(fxml.contains("onAction=\"#handleSendMessage\""))
        );
    }

    @Test
    void mainMenuFXMLKeepsSuggestedPuzzlesGridWiredToController() throws IOException, NoSuchFieldException {
        String fxml = readResource("/com/example/syntaxio/main-menu.fxml");
        Field field = MainMenuController.class.getDeclaredField("suggestedPuzzlesGrid");

        assertAll(
                () -> assertTrue(fxml.contains("fx:id=\"suggestedPuzzlesGrid\"")),
                () -> assertEquals(GridPane.class, field.getType())
        );
    }

    @Test
    void mainMenuCssKeepsChatBubbleStyles() throws IOException {
        String css = readResource("/com/example/syntaxio/css/main-menu.css");

        assertAll(
                () -> assertTrue(css.contains(".bot-message-row")),
                () -> assertTrue(css.contains(".user-message-row")),
                () -> assertTrue(css.contains(".bot-bubble")),
                () -> assertTrue(css.contains(".user-bubble"))
        );
    }

    @Test
    void mainMenuCssKeepsSuggestedPuzzleCardStyles() throws IOException {
        String css = readResource("/com/example/syntaxio/css/main-menu.css");

        assertAll(
                () -> assertTrue(css.contains(".suggested-puzzle-card")),
                () -> assertTrue(css.contains(".suggested-puzzle-title")),
                () -> assertTrue(css.contains(".suggested-puzzle-description")),
                () -> assertTrue(css.contains(".suggested-puzzle-meta"))
        );
    }

    private String readResource(String path) throws IOException {
        try (InputStream stream = MainMenuControllerTest.class.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing test resource: " + path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
