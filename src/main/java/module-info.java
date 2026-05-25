module com.example.syntaxio {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive java.sql;
    requires java.compiler;
    requires ollama4j;

    opens com.example.syntaxio.ui to javafx.fxml;
    exports com.example.syntaxio.ui;
    exports com.example.syntaxio.ui.controller;
    opens com.example.syntaxio.ui.controller to javafx.fxml;
    exports com.example.syntaxio.database;
    exports com.example.syntaxio.model;
    exports com.example.syntaxio.ai;
}
