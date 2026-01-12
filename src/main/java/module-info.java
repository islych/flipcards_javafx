module com.myapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.base;
    requires java.sql;

    opens com.myapp.controllers to javafx.fxml;
    opens com.myapp.models to javafx.fxml, javafx.base;
    exports com.myapp;
    opens com.myapp.utils to javafx.base, javafx.fxml;
}