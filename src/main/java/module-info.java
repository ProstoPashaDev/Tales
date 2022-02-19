module com.example.russiantales {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires com.h2database;


    opens com.example.russiantales to javafx.fxml;
    exports com.example.russiantales;
}