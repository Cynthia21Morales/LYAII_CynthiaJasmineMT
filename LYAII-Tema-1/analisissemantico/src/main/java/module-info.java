module com.cynthiasystems {
    requires javafx.controls;
    requires javafx.fxml;
    exports com.cynthiasystems;
    exports com.cynthiasystems.controller;
    opens com.cynthiasystems.controller to javafx.fxml;
}