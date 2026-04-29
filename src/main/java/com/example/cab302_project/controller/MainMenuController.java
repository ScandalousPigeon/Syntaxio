package com.example.cab302_project.controller;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class MainMenuController {

    @FXML
    private VBox popoutMenu;

    //@FXML
    //private Region dimOverlay;

    private boolean menuOpen = false;

    private static final double MENU_WIDTH = 330;

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
        //dimOverlay.setVisible(true);
        menuOpen = true;
        TranslateTransition slideIn = new TranslateTransition(Duration.millis(220), popoutMenu);
        slideIn.setToX(0);
        slideIn.play();
    }

    @FXML
    private void closeMenu() {
        menuOpen = false;
        TranslateTransition slideOut = new TranslateTransition(Duration.millis(220), popoutMenu);
        slideOut.setToX(-MENU_WIDTH);

        //slideOut.setOnFinished(event -> dimOverlay.setVisible(false));

        slideOut.play();
    }
}