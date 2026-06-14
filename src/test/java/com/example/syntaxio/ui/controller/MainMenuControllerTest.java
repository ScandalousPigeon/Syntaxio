package com.example.syntaxio.ui.controller;

import javafx.event.ActionEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

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
                () -> assertTrue(fxml.contains("fx:id=\"assistantStatusText\"")),
                () -> assertTrue(fxml.contains("text=\"➤\"")),
                () -> assertTrue(fxml.contains("Ask about Java, algorithms")),
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
    void mainMenuFXMLKeepsInProgressCardWiredToController() throws IOException {
        String fxml = readResource("/com/example/syntaxio/main-menu.fxml");

        assertAll(
                () -> assertTrue(fxml.contains("fx:id=\"inProgressContent\"")),
                () -> assertTrue(fxml.contains("fx:id=\"inProgressEmptyState\"")),
                () -> assertTrue(fxml.contains("fx:id=\"inProgressTitleLabel\"")),
                () -> assertTrue(fxml.contains("fx:id=\"inProgressDifficultyLabel\"")),
                () -> assertTrue(!fxml.contains("fx:id=\"inProgressDescriptionLabel\"")),
                () -> assertTrue(fxml.contains("fx:id=\"inProgressMetadataLabel\"")),
                () -> assertTrue(fxml.contains("<?import javafx.scene.layout.Region?>")),
                () -> assertTrue(fxml.contains("onAction=\"#handleContinueInProgress\"")),
                () -> assertTrue(fxml.contains("onAction=\"#handleBrowseChallenges\""))
        );
    }

    @Test
    void mainMenuFXMLKeepsRootPaneWiredForWindowMaximize() throws IOException, NoSuchFieldException {
        String fxml = readResource("/com/example/syntaxio/main-menu.fxml");
        Field field = MainMenuController.class.getDeclaredField("rootPane");

        assertAll(
                () -> assertTrue(fxml.contains("fx:id=\"rootPane\"")),
                () -> assertEquals(StackPane.class, field.getType())
        );
    }

    @Test
    void mainMenuFXMLKeepsProfileCardWiredToController() throws IOException, NoSuchFieldException {
        String fxml = readResource("/com/example/syntaxio/main-menu.fxml");

        assertAll(
                () -> assertTrue(fxml.contains("fx:id=\"profileLevelBadge\"")),
                () -> assertTrue(fxml.contains("fx:id=\"profileUsernameText\"")),
                () -> assertTrue(fxml.contains("fx:id=\"profileStreakText\"")),
                () -> assertTrue(fxml.contains("fx:id=\"profileXpText\"")),
                () -> assertTrue(fxml.contains("fx:id=\"profileXpProgressBar\"")),
                () -> assertTrue(fxml.contains("fx:id=\"menuLevelText\"")),
                () -> assertEquals(Label.class,
                        MainMenuController.class.getDeclaredField("profileLevelBadge").getType()),
                () -> assertEquals(Text.class,
                        MainMenuController.class.getDeclaredField("profileUsernameText").getType()),
                () -> assertEquals(Text.class,
                        MainMenuController.class.getDeclaredField("profileStreakText").getType()),
                () -> assertEquals(Text.class,
                        MainMenuController.class.getDeclaredField("profileXpText").getType()),
                () -> assertEquals(ProgressBar.class,
                        MainMenuController.class.getDeclaredField("profileXpProgressBar").getType()),
                () -> assertEquals(Text.class,
                        MainMenuController.class.getDeclaredField("menuLevelText").getType())
        );
    }

    @Test
    void mainMenuControllerKeepsInProgressHandlersAvailable() throws NoSuchMethodException {
        assertAll(
                () -> assertHandlerExists("handleContinueInProgress"),
                () -> assertHandlerExists("handleBrowseChallenges")
        );
    }

    @Test
    void mainMenuCssKeepsChatBubbleStyles() throws IOException {
        String css = readResource("/com/example/syntaxio/css/main-menu.css");

        assertAll(
                () -> assertTrue(css.contains(".bot-message-row")),
                () -> assertTrue(css.contains(".user-message-row")),
                () -> assertTrue(css.contains(".bot-message-meta")),
                () -> assertTrue(css.contains(".user-message-meta")),
                () -> assertTrue(css.contains(".bot-bubble")),
                () -> assertTrue(css.contains(".user-bubble")),
                () -> assertTrue(css.contains(".send-button:disabled"))
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

    @Test
    void mainMenuCssKeepsInProgressCardStyles() throws IOException {
        String css = readResource("/com/example/syntaxio/css/main-menu.css");

        assertAll(
                () -> assertTrue(css.contains(".in-progress-title")),
                () -> assertTrue(css.contains(".in-progress-description")),
                () -> assertTrue(css.contains(".continue-button")),
                () -> assertTrue(css.contains(".secondary-action-button"))
        );
    }

    private String readResource(String path) throws IOException {
        try (InputStream stream = MainMenuControllerTest.class.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing test resource: " + path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void assertHandlerExists(String methodName) throws NoSuchMethodException {
        Method method = MainMenuController.class.getDeclaredMethod(methodName, ActionEvent.class);
        assertTrue(method.getReturnType().equals(void.class));
    }
}
