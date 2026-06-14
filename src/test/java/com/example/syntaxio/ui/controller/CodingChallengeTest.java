package com.example.syntaxio.ui.controller;

import com.example.syntaxio.database.SqliteChallengeDAO;
import com.example.syntaxio.model.Challenge;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodingChallengeTest {

    private static final String CODING_CHALLENGE_FXML = "/com/example/syntaxio/coding-challenge.fxml";

    @Test
    void topbarButtonsRemainWiredToControllerHandlers() throws IOException {
        String fxml = readResource(CODING_CHALLENGE_FXML);

        assertAll(
                () -> assertTopbarButton(fxml, "backButton", "Back", "#onBack"),
                () -> assertTopbarButton(fxml, "runButton", "Run", "#onRun"),
                () -> assertTopbarButton(fxml, "submitButton", "Submit", "#onSubmit"),
                () -> assertTimerLabel(fxml)
        );
    }

    @Test
    void topbarControllerKeepsButtonHandlersAvailable() throws NoSuchMethodException {
        assertAll(
                () -> assertHandlerExists("onBack", ActionEvent.class),
                () -> assertHandlerExists("onRun"),
                () -> assertHandlerExists("onSubmit", ActionEvent.class)
        );
    }

    @Test
    void backButtonNavigatesToMainMenu() throws Exception {
        CodingChallengeController controller = new CodingChallengeController();
        CapturingScreenSwitcher screenSwitcher = new CapturingScreenSwitcher();
        controller.setScreenSwitcher(screenSwitcher);

        invokeActionHandler(controller, "onBack", new ActionEvent());

        assertAll(
                () -> assertEquals("/com/example/syntaxio/main-menu.fxml", screenSwitcher.fxmlPath),
                () -> assertEquals(1200, screenSwitcher.width),
                () -> assertEquals(1150, screenSwitcher.height)
        );
    }

    @Test
    void selectedChallengeDescriptionCanBeLoadedFromDatabase() {
        SqliteChallengeDAO challengeDAO = new SqliteChallengeDAO();

        Challenge challenge = challengeDAO.getChallengeById("ch-001");

        assertAll(
                () -> assertNotNull(challenge),
                () -> assertFalse(challenge.getDescription().isBlank()),
                () -> assertTrue(challenge.getDescription().contains("Method signature"))
        );
    }

    @Test
    void descriptionTabKeepsDatabaseDescriptionDisplayArea() throws IOException {
        String fxml = readResource(CODING_CHALLENGE_FXML);
        String descriptionTab = openingTagFor(fxml, "descriptionTab");
        String descriptionArea = openingTagFor(fxml, "descriptionArea");

        assertAll(
                () -> assertTrue(descriptionTab.contains("text=\"Description\"")),
                () -> assertTrue(descriptionArea.startsWith("<TextArea ")),
                () -> assertTrue(descriptionArea.contains("editable=\"false\"")),
                () -> assertTrue(descriptionArea.contains("wrapText=\"true\""))
        );
    }

    @Test
    void aiAssistantTabKeepsChatControlsWiredToController() throws Exception {
        String fxml = readResource(CODING_CHALLENGE_FXML);

        assertAll(
                () -> assertTrue(openingTagFor(fxml, "descriptionTab").contains("onAction=\"#showDescriptionTab\"")),
                () -> assertTrue(openingTagFor(fxml, "aiAssistantTab").contains("onAction=\"#showAssistantTab\"")),
                () -> assertTrue(openingTagFor(fxml, "assistantInput").contains("onAction=\"#handleAssistantSend\"")),
                () -> assertTrue(openingTagFor(fxml, "assistantSendButton").contains("onAction=\"#handleAssistantSend\"")),
                () -> assertEquals(ToggleButton.class,
                        CodingChallengeController.class.getDeclaredField("descriptionTab").getType()),
                () -> assertEquals(ToggleButton.class,
                        CodingChallengeController.class.getDeclaredField("aiAssistantTab").getType()),
                () -> assertEquals(VBox.class,
                        CodingChallengeController.class.getDeclaredField("assistantPane").getType()),
                () -> assertEquals(ScrollPane.class,
                        CodingChallengeController.class.getDeclaredField("assistantScrollPane").getType()),
                () -> assertEquals(VBox.class,
                        CodingChallengeController.class.getDeclaredField("assistantMessages").getType()),
                () -> assertEquals(TextField.class,
                        CodingChallengeController.class.getDeclaredField("assistantInput").getType()),
                () -> assertEquals(Button.class,
                        CodingChallengeController.class.getDeclaredField("assistantSendButton").getType())
        );
    }

    @Test
    void aiAssistantControllerKeepsHandlersAvailable() throws NoSuchMethodException {
        assertAll(
                () -> assertHandlerExists("showDescriptionTab"),
                () -> assertHandlerExists("showAssistantTab"),
                () -> assertHandlerExists("handleAssistantSend")
        );
    }

    @Test
    void timerFormatsElapsedSecondsAsStopwatchTime() {
        assertAll(
                () -> assertEquals("00:00", CodingChallengeController.formatElapsedTime(0)),
                () -> assertEquals("00:09", CodingChallengeController.formatElapsedTime(9)),
                () -> assertEquals("01:05", CodingChallengeController.formatElapsedTime(65)),
                () -> assertEquals("12:34", CodingChallengeController.formatElapsedTime(754))
        );
    }

    @Test
    void controllerKeepsTimerDisplayInjected() throws NoSuchFieldException {
        Field field = CodingChallengeController.class.getDeclaredField("timeIndicator");

        assertEquals(Label.class, field.getType());
    }

    private static void assertTopbarButton(
            String fxml,
            String fxId,
            String text,
            String onAction
    ) {
        String buttonTag = openingTagFor(fxml, fxId);

        assertAll(
                () -> assertTrue(buttonTag.startsWith("<Button "), fxId + " should be a Button"),
                () -> assertTrue(buttonTag.contains("text=\"" + text + "\""),
                        fxId + " should show text \"" + text + "\""),
                () -> assertTrue(buttonTag.contains("onAction=\"" + onAction + "\""),
                        fxId + " should call " + onAction),
                () -> assertTrue(buttonTag.contains("mnemonicParsing=\"false\""),
                        fxId + " should not use keyboard mnemonics")
        );
    }

    private static void assertTimerLabel(String fxml) {
        String timerTag = openingTagFor(fxml, "timeIndicator");

        assertAll(
                () -> assertTrue(timerTag.startsWith("<Label "), "timeIndicator should be a Label"),
                () -> assertTrue(timerTag.contains("text=\"00:00\""),
                        "Timer should display 00:00 before the stopwatch starts"),
                () -> assertTrue(timerTag.contains("styleClass=\"timer-box\""),
                        "Timer should keep the topbar timer styling")
        );
    }

    private static void assertHandlerExists(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = CodingChallengeController.class.getDeclaredMethod(methodName, parameterTypes);
        assertEquals(void.class, method.getReturnType());
    }

    private static void invokeActionHandler(
            CodingChallengeController controller,
            String methodName,
            ActionEvent event
    ) throws Exception {
        Method method = CodingChallengeController.class.getDeclaredMethod(methodName, ActionEvent.class);
        method.setAccessible(true);
        method.invoke(controller, event);
    }

    private static String openingTagFor(String fxml, String fxId) {
        String fxIdAttribute = "fx:id=\"" + fxId + "\"";
        int fxIdIndex = fxml.indexOf(fxIdAttribute);
        assertTrue(fxIdIndex >= 0, "Missing element with " + fxIdAttribute);

        int tagStart = fxml.lastIndexOf('<', fxIdIndex);
        int tagEnd = fxml.indexOf('>', fxIdIndex);
        assertTrue(tagStart >= 0 && tagEnd > tagStart, "Malformed element with " + fxIdAttribute);

        return fxml.substring(tagStart, tagEnd + 1);
    }

    private static String readResource(String path) throws IOException {
        try (InputStream stream = CodingChallengeTest.class.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing test resource: " + path);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static class CapturingScreenSwitcher implements CodingChallengeController.ScreenSwitcher {
        private String fxmlPath;
        private double width;
        private double height;

        @Override
        public void switchScreen(ActionEvent event, String fxmlPath, double width, double height) {
            this.fxmlPath = fxmlPath;
            this.width = width;
            this.height = height;
        }
    }
}
