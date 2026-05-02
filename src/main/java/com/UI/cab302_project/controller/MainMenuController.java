package com.UI.cab302_project.controller;

import com.Database.SessionManager;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import static com.UI.cab302_project.util.ScreenManager.switchScreen;

import java.io.IOException;

public class MainMenuController {

    @FXML
    private VBox popoutMenu;

    @FXML
    private HBox logoutButton;

    //@FXML
    //private Region dimOverlay;

    private boolean menuOpen = false;
    private TranslateTransition currentAnimation;

    @FXML
    private void initialize() {
        //dimOverlay.setVisible(false);

        // hide it so it doesn't flash on screen
        popoutMenu.setVisible(false);

        // wait until JavaFX has calculated the menu width
        Platform.runLater(() -> {
            popoutMenu.setTranslateX(popoutMenu.getWidth());
            popoutMenu.setVisible(true);
        });

        // if the menu width changes while closed, keep it offscreen
        popoutMenu.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            if (!menuOpen) {
                popoutMenu.setTranslateX(newWidth.doubleValue());
            }
        });
    }

    @FXML
    private void openMenu() {
        if (currentAnimation != null) currentAnimation.stop();
        menuOpen = true;
        currentAnimation = new TranslateTransition(Duration.millis(220), popoutMenu);
        currentAnimation.setToX(0);
        currentAnimation.play();
    }

    @FXML
    private void closeMenu() {
        if (currentAnimation != null) currentAnimation.stop();
        menuOpen = false;
        currentAnimation = new TranslateTransition(Duration.millis(220), popoutMenu);
        currentAnimation.setToX(popoutMenu.getWidth());
        currentAnimation.play();
    }

    @FXML
    private void handleLogOut(MouseEvent event) throws IOException {
        SessionManager.getInstance().logout();
        switchScreen(event, "/com/example/cab302_project/login-screen.fxml", 350, 650);
    }
}