package com.example.syntaxio.ui.controller;

import com.example.syntaxio.database.SqliteChallengeDAO;
import com.example.syntaxio.model.Challenge;
import javafx.event.ActionEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
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
                () -> assertTopbarButton(fxml, "submitButton", "Submit", "#onSubmit")
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
