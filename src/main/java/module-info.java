module com.example.cab302_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.example.cab302_project to javafx.fxml;
    exports com.example.cab302_project;
    exports com.example.cab302_project.controller;
    opens com.example.cab302_project.controller to javafx.fxml;
}