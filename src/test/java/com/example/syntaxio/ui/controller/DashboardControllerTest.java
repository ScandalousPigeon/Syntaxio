package com.example.syntaxio.ui.controller;

import javafx.event.ActionEvent;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.lang.reflect.Method;

import javax.xml.parsers.DocumentBuilderFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DashboardControllerTest {

    private static final String DASHBOARD_FXML = "/com/example/syntaxio/dashboard.fxml";

    @Test
    void backToHomeButtonIsWiredToControllerHandler() throws Exception {
        Element backButton = findButtonByText("Back to Home");
        Method handler = DashboardController.class.getDeclaredMethod("onBackToHome", ActionEvent.class);

        assertNotNull(backButton, "Dashboard should have a Back to Home button");
        assertEquals("#onBackToHome", backButton.getAttribute("onAction"));
        assertEquals(void.class, handler.getReturnType());
    }

    private static Element findButtonByText(String text) throws Exception {
        Document document = readFxml(DASHBOARD_FXML);
        NodeList buttons = document.getElementsByTagName("Button");

        for (int i = 0; i < buttons.getLength(); i++) {
            Element button = (Element) buttons.item(i);
            if (button.getAttribute("text").contains(text)) {
                return button;
            }
        }

        return null;
    }

    private static Document readFxml(String path) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        try (InputStream stream = DashboardControllerTest.class.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing test resource: " + path);
            return factory.newDocumentBuilder().parse(stream);
        }
    }
}
