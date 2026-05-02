module com.example.cab302_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires transitive java.sql;
    requires java.compiler;

    opens com.UI.cab302_project to javafx.fxml;
    exports com.UI.cab302_project;
    exports com.UI.cab302_project.controller;
    opens com.UI.cab302_project.controller to javafx.fxml;
    exports com.Database;
    exports com.Model;
    exports com.AI;
}